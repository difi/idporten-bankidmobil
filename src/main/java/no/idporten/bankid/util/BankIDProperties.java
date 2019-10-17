package no.idporten.bankid.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Contains constants for names in the BankID module properties-file.
 */
@Component
@Getter
public class BankIDProperties {
        public static final String BANKID_LOCALE_ENGLISH = "English";
        public static final String BANKID_LOCALE_NORWEGIAN = "Norwegian";
        public static final String HTTP_SESSION_AUTH_TYPE = "session.authenticationType";
        public static final String HTTP_SESSION_CLIENT_TYPE = "session.clientType";
        public static final String HTTP_SESSION_STATE = "session.state";

        @Value("${bankid.servlet.address}")
        private String bankIdServletAddress;

        @Value("${bankid.response.url}")
        private String bankIdResponseUrl;

        @Value("${bankid.action}")
        private String bankIdAction;

        @Value("${bankid.clienttype}")
        private String bankIdClienttype;

        @Value("${bankid.keystorefile}")
        private String bankIdMerchantKeystore;

        @Value("${bankid.webaddress.hostname}")
        private String bankIdServerPublicHostname;

        @Value("${bankid.webaddress.ip}")
        private String bankIdPublicIP;

        @Value("${bankid.grantedpolicies}")
        private String bankIdGrantedPolicies;

        @Value("${bankid.keystorefile.password}")
        private String bankIdMerchantKeystorePassword;

        @Value("${bankid.merchant.name}")
        private String bankIdMerchantName;

        @Value("${bankid.timeout}")
        private String bankIdTimeout;

        @Value("${bankid.reset}")
        private Boolean resettBankId;

        @Value("${bankid.response.servlet.url}")
        private String bankIdResponseServletUrl;

        @Value("${bankid.event.emitter}")
        private String bankIdEventEmitter;

        @Value("${bankid.serverid}")
        private String bankIdServerId;

        @Value("${bankid.logging.enabled}")
        private Boolean bankIdLoggingEnabled;

        @Value("${bankid.logging.propfile}")
        private String bankIdLoggingPropFile;

        @Value("${bankid.logging.categoryname}")
        private String bankIdLoggingCategoryName;

        @Value("${bankid.cors.allow_origin}")
        private String bankIdCorsAllowOrigin;

        @Value("${bankid.suppress_broadcast}")
        private String bankIdSuppressBroadcast;

        //bankid.cors.allow_origin=https://csfe-preprod.bankid.no

        private Boolean shouldResetBankId;

        /** A private constructor to prevent someone to create a object from this class. */
        private BankIDProperties() {
        }

        public boolean shouldResetBankId() {
                return shouldResetBankId == null ? resettBankId : shouldResetBankId;
        }

        public void resetBankIdToFalse() {
                shouldResetBankId = false;
        }

        public boolean isBankIdLoggingEnabled() {
                return bankIdLoggingEnabled;
        }

        public String getLocale(HttpServletRequest request) {
                String locale = LanguageUtils.getLanguage(request);
                if (LanguageUtils.LANGUAGE_EN.equals(locale)){
                        return BANKID_LOCALE_ENGLISH;
                }else {
                        return BANKID_LOCALE_NORWEGIAN;
                }

        }
}