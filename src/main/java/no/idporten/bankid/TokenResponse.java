package no.idporten.bankid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
class TokenResponse {
    @JsonProperty("ssn")
    private String ssn;

    @JsonProperty("ocsp")
    private String ocsp;

    //seconds
    @JsonProperty("expires_in")
    private long expiresIn;

}
