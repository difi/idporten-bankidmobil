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
        String code = tokenRequest.getCode();
        String sid = bankIDCache.getSID(code);
        if (sid == null) {
            return ResponseEntity.notFound().build();
        }
        String ssn = bankIDCache.getSSN(sid);
        byte[] ocsp = bankIDCache.getOCSP(sid);

        final TokenResponse tokenResponse = new TokenResponse(ssn,
                Base64.encodeBase64String(ocsp), expirySeconds);

        bankIDCache.removeSession(sid);
        bankIDCache.removeUuidSID(code);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping(
            value = "/test",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ResponseEntity testAuthorizationCodeGrant(TokenRequest tokenRequest) {
        String code = tokenRequest.getCode();


        final TokenResponse tokenResponse = new TokenResponse("324243",
                Base64.encodeBase64String("ocsp".getBytes()), expirySeconds);

        return ResponseEntity.ok(tokenResponse);
    }



}
