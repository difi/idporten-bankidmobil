package no.idporten.bankid;

import no.difi.kontaktinfo.validation.SsnValidator;
import no.idporten.bankid.util.BankIDProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@Controller
@RequestMapping
public class BankIDMobilResponseServlet extends HttpServlet {

   private static final long serialVersionUID = 3734847325014355167L;

    private static final String SERVICE_PARAMETER_NAME = "service";
    private static final String GOTO_PARAMETER_NAME = "goto";
    private static final String FORCE_AUTH_PARAMETER_NAME = "ForceAuth";
    private static final String CHARSET_PARAMETER_NAME = "gx_charset";
    private static final String LOCALE_PARAMETER_NAME = "locale";
    private static final String SERVER_ID_PARAMETER_NAME = "serverid";
    private static final String SID_PARAMETER_NAME = "code";
    private static final String BANKID_RESPONSE_SERVICE = "BankIDMobilEkstern";

    private BankIDCache bankIDCache;
    private BankIDProperties bankIDProperties;
    private final Logger log = LoggerFactory.getLogger(BankIDMobilResponseServlet.class);

    BankIDMobilResponseServlet(BankIDCache bankIDCache, BankIDProperties bankIDProperties) {
        this.bankIDCache = bankIDCache;
        this.bankIDProperties = bankIDProperties;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/bidresponse")
    protected void service(HttpServletRequest request,
                           @RequestParam(required = false) String idpError,
                           HttpServletResponse response) throws IOException {
        String sid = (String) request.getSession().getAttribute("sid");
        log.debug("/bidresponse status: " + request.getSession().getAttribute(BankIDProperties.HTTP_SESSION_STATE)
                + " idperror " + idpError + " status: " + bankIDCache.getMobileStatus(sid));
        String code = (BankIDMobileStatus.FINISHED == bankIDCache.getMobileStatus(sid)) ? sid : "";
        String service;
        if (idpError == null) {
            if (SsnValidator.isValid(bankIDCache.getSSN(sid))) {
                service = BANKID_RESPONSE_SERVICE;
            } else {
                service = getStartServiceForError(request);
            }
        } else {
            service = getStartServiceForError(request);
            log.error("BankIDResponseServlet User restarting due to error: " + idpError);
        }

        String url = buildUrl(code, service, request);
        renderHelpingPage(response, url);

    }

    private String getStartServiceForError(HttpServletRequest request) {
        Object startService = request.getSession().getAttribute("start-service");
        return startService == null ? "null" : startService.toString();
    }

    private String buildUrl(String code, String service, HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            return UriComponentsBuilder.newInstance()
                       .uri(new URI((String) session.getAttribute("redirectUrl")))
                       .queryParam(SID_PARAMETER_NAME, code)
                       .queryParam(FORCE_AUTH_PARAMETER_NAME, session.getAttribute("forceAuth"))
                       .queryParam(CHARSET_PARAMETER_NAME, request.getSession().getAttribute("gx_charset"))
                       .queryParam(LOCALE_PARAMETER_NAME, request.getSession().getAttribute("locale"))
                       .queryParam(GOTO_PARAMETER_NAME, request.getSession().getAttribute("goto"))
                       .queryParam(SERVICE_PARAMETER_NAME, service)
                       .queryParam(SERVER_ID_PARAMETER_NAME, bankIDProperties.getBankIdServerId())
                       .build()
                       .toUriString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void renderHelpingPage(HttpServletResponse response, String url) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append(top(url));
        result.append(footer());
        response.setContentType(getContentType());
        response.getWriter().append(result);
    }

    private String top(String url) {
        return "<html>" +
                "<head><title>Submit This Form</title></head>" +
                "<body onload=\"javascript:document.forms[0].submit()\">" +
                "<form target=\"_parent\" method=\"post\" action=\"" + url + "\">";
    }

    private String footer() {
        return "<noscript><input type=\"submit\" value=\"Click to redirect\"></noscript>" +
                "</form>" +
                "</body>" +
                "</html>";
    }

    private String getContentType() {
        return "text/html";
    }

}
