package no.idporten.bankid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Validerer code challenge og returnerer token med fødselsnummer
 */
@Controller
@RequestMapping(value = "/token")
@Slf4j
@RequiredArgsConstructor
public class BankIDJSTokenController {

    private final BankIDCache bankIDCache;
    private final static int expirySeconds = 600;

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ResponseEntity handleAuthorizationCodeGrant(TokenRequest tokenRequest) {
        String sid = tokenRequest.getCode();

        String ssn = bankIDCache.getSSN(sid);
        byte[] ocsp = bankIDCache.getOCSP(sid);

        if (ssn == null) {
            return ResponseEntity.notFound().build();
        }

        final TokenResponse tokenResponse = new TokenResponse(ssn,
                Base64.encodeBase64String(ocsp), expirySeconds);

        bankIDCache.removeSession(sid);
        return ResponseEntity.ok(tokenResponse);
    }

}
