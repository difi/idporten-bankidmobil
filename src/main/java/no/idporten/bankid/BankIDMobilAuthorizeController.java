package no.idporten.bankid;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.bbs.server.vos.MobileInfo;
import no.bbs.server.vos.TransactionAndStatus;
import no.difi.kontaktinfo.validation.BirthDateValidator;
import no.difi.kontaktinfo.validation.MobileValidator;
import no.idporten.bankid.util.BankIDProperties;
import no.idporten.bankid.util.ValidatorUtil;
import no.idporten.domain.auth.AuthType;
import no.idporten.ui.impl.IDPortenButtonType;
import no.idporten.ui.impl.IDPortenFeedbackType;
import no.idporten.ui.impl.IDPortenInputType;
import org.apache.commons.lang.StringUtils;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Logic implementation of BankID web client (BankID 2.0)  module.
 */
@Controller
@RequestMapping(value = "/authorize")
@Slf4j
@Getter
@RequiredArgsConstructor
public class BankIDMobilAuthorizeController {
    protected static final int STATE_AUTHENTICATED = -1;
    protected static final int STATE_USERDATA = 1;
    protected static final int STATE_VERIFICATION_CODE = 2;
    protected static final int STATE_ERROR = 3;

    private static final String SERVICE_PARAMETER_NAME = "service";

    private MobileInfo mobileInfo;

    private final BankIDProperties bankIdProperties;
    private final BankIDFacadeWrapper bankIdFacadeWrapper;

    private final BankIDCache bankIDCache;

    @Value("${idporten.redirecturl}")
    private String redirectUrl;

    @Value("${eventsource.enabled: true}")
    private boolean eventSourceEnabled;


    @GetMapping
    public ModelAndView doGet(HttpServletRequest request) {
        request.getSession().setAttribute(BankIDProperties.HTTP_SESSION_AUTH_TYPE, AuthType.BANKID_MOBILE);
        request.getSession().setAttribute("redirectUrl", request.getParameter("redirectUrl"));
        request.getSession().setAttribute("ForceAuth", request.getParameter("ForceAuth"));
        request.getSession().setAttribute("gx_charset", request.getParameter("gx_charset"));
        request.getSession().setAttribute("locale", request.getParameter("locale"));
        request.getSession().setAttribute("goto", request.getParameter("goto"));
        request.getSession().setAttribute("service", request.getParameter("goto"));
        request.getSession().setAttribute("start-service", request.getParameter("start-service"));
        request.getSession().setAttribute("eventsourceEnabled", eventSourceEnabled);
        setSessionState(request, STATE_USERDATA);
        initMobileInfo(request);

        return new ModelAndView("bankidmobil_enter_userdata");
    }

    @PostMapping
    public ModelAndView doPost(HttpServletRequest request,
                               HttpServletResponse response) throws URISyntaxException, IOException {
        try {
            if (request.getSession() == null) {
                log.error("Request har ikke session? ");
                return new ModelAndView("bankidmobil_error");
            }
            int state = (int) request.getSession().getAttribute(BankIDProperties.HTTP_SESSION_STATE);
            if (state == STATE_USERDATA) {
                return getNextView(request, handleUserdataInput(request));
            } else if (state == STATE_VERIFICATION_CODE) {
                return getNextView(request, handlePollingFinished(request));
            } else if (state == STATE_ERROR) {
                handleErrorPage();
                return new ModelAndView("bankidmobil_error");
            }
        } catch (LoginException e) {
            UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                    .uri(new URI(bankIdProperties.getBankIdResponseUrl()))
                    .queryParam("code", "")
                    .queryParam(SERVICE_PARAMETER_NAME, getStartServiceForError(request));
            response.sendRedirect(builder.build().toString());
        }
        return new ModelAndView("bankidmobil_error");
    }

    private String getStartServiceForError(HttpServletRequest request) {
        Object startService = request.getSession().getAttribute("start-service");
        return startService == null ? "null" : startService.toString();
    }

    private ModelAndView getNextView(HttpServletRequest request, int state) {
        setSessionState(request, state);
        if (state == STATE_VERIFICATION_CODE) {
            return new ModelAndView("bankidmobil_show_reference");
        } else if (state == STATE_USERDATA) {
            return new ModelAndView("bankidmobil_enter_userdata");
        } else if (state == STATE_ERROR) {
            return new ModelAndView("bankidmobil_error");
        } else if (state == STATE_AUTHENTICATED) {
            return new ModelAndView(redirectUrl);
        } else {
            return new ModelAndView("bankidmobil_error");
        }
    }

    /**
     * Inits mobileInfo object with values that are the same for all authentications with BankID mobile or
     * can be gotten from config/session.
     * @param request
     */
    protected void initMobileInfo(HttpServletRequest request) {
        final MobileInfo mobileInfo = new MobileInfo();
        mobileInfo.setCountryCode("47");
        mobileInfo.setDoSynchronusCommunication(false);
        mobileInfo.setDoAliasCheck(true);
        mobileInfo.setLocale("no_NO");
        mobileInfo.setAction("auth");
        mobileInfo.setSid(request.getSession().getId());
        mobileInfo.setUrl(bankIdProperties.getBankIdServletAddress());
        this.mobileInfo = mobileInfo;
    }

    /**
     * Handles userdata input.
     * Input validation and feedback loop in case of errors.
     * Continues to mobile transaction if input is OK.
     * @param request
     * @return
     * @throws LoginException
     */
    protected int handleUserdataInput(HttpServletRequest request) throws LoginException {
        if (isButtonPushed(request, IDPortenButtonType.CANCEL)) {
            throw new BankIDMobileCancelException();
        }
        final String inputMobileNumber = validateMobileNumber(request);
        final String inputBirthDate = validateDate(request);
        if (inputMobileNumber == null || inputBirthDate == null) {
            if (inputMobileNumber == null && inputBirthDate == null) {
                setFeedback(request, IDPortenFeedbackType.WARNING, "no.idporten.module.bankid.input.birthdate_mobile.error");
            }
            request.setAttribute("mobileNumber", inputMobileNumber != null ? ESAPI.encoder().encodeForHTMLAttribute(inputMobileNumber) : "");
            request.setAttribute("birthDate", inputBirthDate != null ? ESAPI.encoder().encodeForHTMLAttribute(inputBirthDate) : "");
            request.setAttribute("eventEmitterUrl", bankIdProperties.getBankIdEventEmitter());
            return STATE_USERDATA;
        }
        getMobileInfo().setPhoneNumber(inputMobileNumber);
        getMobileInfo().setPhoneAlias(inputBirthDate);
        return prepareMobileTransaction(request);
    }

    /**
     * Prepares mobile transaction.
     * Generates merchant reference and exposes on request.
     * Request mobile action with mobileInfo built from defaults in init, user input and merchant reference.
     *
     * @param request
     * @return
     * @throws LoginException
     */
    protected int prepareMobileTransaction(HttpServletRequest request) throws LoginException {
        try {
            final String merchantReference = bankIdFacadeWrapper.getFacade().generateMerchantReference("no_NO");
            getMobileInfo().setMerchantReference(merchantReference);
            final TransactionAndStatus mobileSession = bankIdFacadeWrapper.getFacade().requestMobileAction(getMobileInfo());
            mobileSession.setStatusCode("0");
            mobileSession.setTransactionReference(merchantReference);
            if ("0".equalsIgnoreCase(mobileSession.getStatusCode())) {
                bankIDCache.putMobileStatus(request.getSession().getId(), BankIDMobileStatus.WAIT);
                request.setAttribute("code", merchantReference);
                return STATE_VERIFICATION_CODE;
            } else {
                log.error("Failed to generate merchant reference and request mobile action " + toStringMobileSession(mobileSession));
                return prepareErrorPage(mobileSession.getStatusCode(), request);
            }
        } catch (Exception e) {
            log.error("Failed to generate merchant reference and request mobile action", e);
            return prepareErrorPage(null, request);
        }
    }

    /**
     * Handles polling.  Polling is either complete or user has cancelled.
     *
     * @param request
     * @return authenticated if authentication complete and OK
     * @throws LoginException
     */
    protected int handlePollingFinished(HttpServletRequest request) throws LoginException {
        if (isButtonPushed(request, IDPortenButtonType.CANCEL)) {
            throw new BankIDMobileCancelException();
        }
        if (BankIDMobileStatus.FINISHED.equals(bankIDCache.getMobileStatus(request.getSession().getId()))) {
            log.debug("STATE_AUTHENTICATED " + request.getSession().getId());
            return STATE_AUTHENTICATED;
        } else {
            log.debug("Error - STATE_AUTHENTICATED " + request.getSession().getId());
            return prepareErrorPage(null, request);
        }
    }

    protected int prepareErrorPage(String errorCode, HttpServletRequest request) {
        if (StringUtils.isNotEmpty(errorCode)) {
            request.setAttribute("errorCode", errorCode);
        }
        return STATE_ERROR;
    }

    /**
     * Handles actions from error page.  The only action is to cancel the authentication and return to eid selector.
     * @return never returns
     * @throws LoginException always returns {@link BankIDMobileCancelException}
     */
    protected int handleErrorPage() throws LoginException {
        throw new BankIDMobileCancelException("User wants to restart authentication");
    }

    private String toStringMobileSession(TransactionAndStatus mobileSession) {
        if(mobileSession == null){
            return "MobileSession is null";
        }
        return "MobileSession: StatusCode[" + mobileSession.getStatusCode() + "], TransactionReference[" + mobileSession.getTransactionReference()+"]";
    }

    protected String validateMobileNumber(final HttpServletRequest request) {
        final String messageId = "no.idporten.module.bankid.input.mobile.error";
        final IDPortenInputType inputType = IDPortenInputType.CONTACTINFO_MOBILE;
        final IDPortenFeedbackType feedbackType = IDPortenFeedbackType.WARNING;
        String input = ValidatorUtil.pruneMobileNumber(getInput(request, inputType));
        if (StringUtils.isEmpty(input)) {
            feedbackInvalidInput(request, feedbackType, inputType, messageId);
            return null;
        }
        input = input.trim();
        if (input.length() != 8 || !MobileValidator.isValid(input)) {
            feedbackInvalidInput(request, feedbackType, inputType, messageId);
            return null;
        }
        return input;
    }

    protected String validateDate(final HttpServletRequest request) {
        final String messageId = "no.idporten.module.bankid.input.birthdate.error";
        final IDPortenInputType inputType = IDPortenInputType.BIRTHDATE;
        String input = getInput(request, inputType);
        if (StringUtils.isEmpty(input)) {
            feedbackInvalidInput(request, IDPortenFeedbackType.WARNING, inputType, messageId);
            return null;
        }
        input = input.trim();
        if (! BirthDateValidator.isValidShortFormat(input)) {
            feedbackInvalidInput(request, IDPortenFeedbackType.WARNING, inputType, messageId);
            return null;
        }
        return input;
    }

    private void feedbackInvalidInput(HttpServletRequest request, IDPortenFeedbackType feedbackType, IDPortenInputType inputType, String messageId) {
        setFeedback(request, inputType, messageId);
        setFeedback(request, feedbackType, messageId);
    }

    private void setFeedback(HttpServletRequest request, Enum feedbackType, String messageId) {
        request.getSession().setAttribute("idporten.feedback." + feedbackType.toString(), messageId);
    }

    private String getInput(HttpServletRequest request, IDPortenInputType inputType) {
        return request.getParameter("idporten.input." + inputType.toString());
    }

    private boolean isButtonPushed(HttpServletRequest request, IDPortenButtonType buttonType) {
        return request.getParameter("idporten.inputbutton." + buttonType.toString()) != null;
    }

    private void setSessionState(HttpServletRequest request, int state) {
        request.getSession().setAttribute(BankIDProperties.HTTP_SESSION_STATE, state);
    }
}
