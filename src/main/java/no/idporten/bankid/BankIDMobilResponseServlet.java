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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;


@Controller
@RequestMapping
public class BankIDMobilResponseServlet extends HttpServlet {

   private static final long serialVersionUID = 3734847325014355167L;

    private static final String SERVICE_PARAMETER_NAME = "service";
    private static final String BANKID_RESPONSE_SERVICE = "BankIDMobilEkstern";

    private BankIDProperties bankIDProperties;
    private BankIDCache bankIDCache;

    private final Logger log = LoggerFactory.getLogger(BankIDMobilResponseServlet.class);

    BankIDMobilResponseServlet(BankIDProperties bankIDProperties, BankIDCache bankIDCache) {
        this.bankIDProperties = bankIDProperties;
        this.bankIDCache = bankIDCache;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/bidresponse")
    protected void service(HttpServletRequest request,
                           @RequestParam(required = false) String idpError,
                           HttpServletResponse response) throws IOException {
        try {
            String sid = request.getSession().getId();
            String uuid = createUUID(sid);
            UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                    .uri(new URI(bankIDProperties.getBankIdResponseUrl()))
                    .queryParam("code", uuid)
                    .queryParam("serverid", bankIDProperties.getBankIdServerId());
            //TODO: Finn this.url og legg på responseURL
            if (idpError == null) {
                if (SsnValidator.isValid(bankIDCache.getSSN(sid))) {
                    builder.queryParam(SERVICE_PARAMETER_NAME, BANKID_RESPONSE_SERVICE);
                } else {
                    builder.queryParam(SERVICE_PARAMETER_NAME, getStartServiceForError(request));
                    request.getSession().setAttribute(SERVICE_PARAMETER_NAME, getStartServiceForError(request));
                }
            } else {
                builder.queryParam(SERVICE_PARAMETER_NAME, getStartServiceForError(request));
                request.getSession().setAttribute(SERVICE_PARAMETER_NAME, getStartServiceForError(request));
                log.error("BankIDResponseServlet User restarting due to error: " + idpError);
            }

            log.debug("code: " + uuid);

            response.sendRedirect(builder.build().toUriString());

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private String getStartServiceForError(HttpServletRequest request) {
        Object startService = request.getSession().getAttribute("start-service");
        return startService == null ? "null" : startService.toString();
    }

    private String createUUID(String sid) {
        String uuid = UUID.randomUUID().toString();
        bankIDCache.putSID(uuid, sid);
        return uuid;
    }
}
