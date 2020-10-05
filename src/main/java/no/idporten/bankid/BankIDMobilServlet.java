package no.idporten.bankid;

import no.bbs.server.constants.JServerConstants;
import no.bbs.server.exception.BIDException;
import no.bbs.server.vos.BIDSessionData;
import no.idporten.bankid.util.BankIDProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@RequestMapping
@Controller
public class BankIDMobilServlet {
    private final Logger log = LoggerFactory.getLogger(BankIDMobilServlet.class);

    private BankIDProperties bankIDProperties;
    private BankIDFacadeWrapper bankIDFacadeWrapper;
    private BankIDCache bankIDCache;

    public BankIDMobilServlet(BankIDProperties bankIDProperties,
                              BankIDFacadeWrapper bankIDFacadeWrapper,
                              BankIDCache bankIDCache) {
        this.bankIDProperties = bankIDProperties;
        this.bankIDFacadeWrapper = bankIDFacadeWrapper;
        this.bankIDCache = bankIDCache;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/bankid")
    protected void service(HttpServletRequest request,
                           @RequestParam String operation,
                           @RequestParam String sid,
                           @RequestParam String encKey,
                           @RequestParam String encData,
                           @RequestParam String encAuth,
                           HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        if ("initAuth".equalsIgnoreCase(operation)) {
            response.getWriter().println(initAuth(operation, sid, encKey, encData, encAuth));
        } else if ("verifyAuth".equalsIgnoreCase(operation)) {
            response.getWriter().println(verifyAuth(operation, sid, encKey, encData, encAuth));
        } else if ("handleError".equalsIgnoreCase(operation)) {
            response.getWriter().println(handleError(operation, sid, encKey, encData, encAuth));
        } else {
            log.warn("Unexpected operation: " + operation);
        }
        response.getWriter().flush();
    }


    /**
     * Checks mobile status for session.
     * Special case for Edge: polling.
     *
     * @param request client requets
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/edge")
    public void handleEdgePolling(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String sid = (String) request.getSession().getAttribute("sid");
        BankIDMobileStatus mobileStatus = bankIDCache.getMobileStatus(sid);
        if (mobileStatus == null) {
            mobileStatus = BankIDMobileStatus.ERROR;
        }
        response.getWriter().print(mobileStatus.name());
        response.getWriter().flush();
    }

    private String initAuth(String operation, String sid, String encKey, String encData, String encAuth) {
        log.debug("initAuth()");
        String traceID = bankIDCache.getTraceId(sid);//request.getSession().getId());
        BIDSessionData sessionData = new BIDSessionData(traceID);

        try {
            String responseToClient = bankIDFacadeWrapper.getFacade().initTransaction(operation, encKey, encData,
                            encAuth, sid, sessionData);
            // store the sessionData in local session store for later use
            bankIDCache.putBIDSessionData(sid, sessionData);
            return responseToClient;
        } catch (BIDException be) {
            log.error(be.toString());
            return handleBIDException(operation, sessionData, be);
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String verifyAuth(String operation, String sid, String encKey, String encData, String encAuth) throws IOException {
        log.debug("verifyAuth()");
        // get the sessionData from local session store
        BIDSessionData sessionData = bankIDCache.getBIDSessionData(sid);
        ArrayList additionalInfos = new ArrayList();
        additionalInfos.add(JServerConstants.LABEL_OID_OCSP_SSN);
        sessionData.setAdditionalInfoList(additionalInfos);

        try {
            bankIDFacadeWrapper.getFacade().verifyTransactionRequest(operation, encKey, encData, encAuth, sid,
                    sessionData);
            log.debug("verifyTransactionRequest ok " + sessionData.getCertificateStatus().getAddInfoSSN() + " sid: " + sid);
            bankIDCache.putOCSP(sid, sessionData.getCertificateStatus().getOcspResponse());
            bankIDCache.putSSN(sid, sessionData.getCertificateStatus().getAddInfoSSN());
            bankIDCache.putMobileStatus(sid, BankIDMobileStatus.FINISHED);
            sendEvent(BankIDMobileStatus.FINISHED, sid);
        } catch (BIDException be) {
            bankIDCache.putMobileStatus(sid, BankIDMobileStatus.ERROR);
            log.error("BankIDServlet.verifyAuth exception " + be);
            log.error(be.toString());
            sendEvent(BankIDMobileStatus.ERROR, sid);
            return handleBIDException(operation, sessionData, be);
        }
        // fra dokumentasjonen: nextUrl required but ignored when BankID on mobile
        sessionData.setNextURL(bankIDProperties.getBankIdResponseServletUrl());
        try {
            return bankIDFacadeWrapper.getFacade().verifyTransactionResponse(sessionData);
            // return the response to the client, and update user session as
            // authenticated
        } catch (BIDException be) {
            log.error(be.toString());
            return handleBIDException(operation, sessionData, be);
        }

    }

    private void sendEvent(BankIDMobileStatus mobileStatus, String sessionId) throws IOException {
        SseEmitter emitter = bankIDCache.getEmitter(sessionId);
        if (emitter != null) {
            emitter.send(mobileStatus);
            bankIDCache.removeEmitter(sessionId);
        }
    }

    @GetMapping(value = "/mobilstatusEmitter/{sid}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSseMvc(@PathVariable String sid) {
        log.debug("kaller mobilstatusEmitter " + sid);
        SseEmitter emitter = bankIDCache.getEmitter(sid);
        if (emitter == null) {
            log.debug("lager ny emitter");
            emitter = new SseEmitter(60000L);
            bankIDCache.putEmitter(sid, emitter);
            emitter.onTimeout(() -> {
                bankIDCache.removeEmitter(sid);
            });
            emitter.onCompletion(() -> {
                bankIDCache.removeEmitter(sid);
            });
            emitter.onError((e) -> {
                bankIDCache.removeEmitter(sid);
            });
        }
        return emitter;
    }

    @GetMapping("/test/{mobilstatus}/{sid}")
    public ResponseEntity testSseEmitter(@PathVariable String mobilstatus, @PathVariable String sid) throws IOException {
        SseEmitter emitter = bankIDCache.getEmitter(sid);
        if (emitter == null) {
            return ResponseEntity.ok("Fant ikke emitter");
        }
        if ("JA".equals(mobilstatus)) {
            sendEvent(BankIDMobileStatus.FINISHED, sid);
        } else {
            sendEvent(BankIDMobileStatus.ERROR, sid);
        }
        return ResponseEntity.ok(mobilstatus);
    }

    /**
     * Handles BIDExceptions and generates error response to BankID client.
     *
     * @param operation operation (used for logging)
     * @param sessionData session data
     * @param exception the API exception
     * @return response string to client
     */
    private String handleBIDException(final String operation, final BIDSessionData sessionData,
                                      final BIDException exception) {
        log.error("Exception for operation [" + operation + "]", exception);
        final int errorCode = exception.getErrorCode();
        sessionData.setErrCode("" + errorCode);
        sessionData.setNextURL(bankIDProperties.getBankIdResponseServletUrl() + "?idpError="+errorCode);
        try {
            return bankIDFacadeWrapper.getFacade().verifyTransactionResponse(sessionData);
        } catch (BIDException e) {
            log.error("Error verifyTransactionResponse.");
            throw new RuntimeException("Cannot handle error");
        }
    }

    private String handleError(String operation, String sid, String encKey, String encData, String encAuth) {
        log.error("handleError()");
        BIDSessionData sessionData = bankIDCache.getBIDSessionData(sid);

        if (sessionData == null) {
            log.error("Error getting session");
            throw new RuntimeException("No BankID session associated with httpSession");
        }
        try {
            sessionData.setNextURL(bankIDProperties.getBankIdResponseServletUrl() + "?idpError=");
            bankIDFacadeWrapper.getFacade().verifyTransactionRequest(operation, encKey, encData, encAuth, sid,
                            sessionData);
            final String errCode = sessionData.getErrCode();
            log.error("Error code from BID client [" + errCode + "]");
            sessionData.setNextURL(bankIDProperties.getBankIdResponseServletUrl() + "?idpError="+errCode);
            return bankIDFacadeWrapper.getFacade().verifyTransactionResponse(sessionData);
        } catch (BIDException e) {
            return handleBIDException(operation, sessionData, e);
        }
    }

}
