package no.idporten.bankid;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class TokenRequest {

    @JsonCreator
    public TokenRequest(String code) {
        this.code = code;
    }

    @JsonProperty("code")
    public String code;
}
