package no.idporten.bankid;

import no.bbs.server.exception.BIDException;
import no.bbs.server.implementation.BIDFacade;
import no.bbs.server.implementation.BIDFactory;
import no.bbs.server.vos.MerchantConfig;
import no.idporten.bankid.util.BankIDProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class initializing and holding reference to BankID Server API.
 */
@Component
public class BankIDFacadeWrapper {

    private final static Logger log = LoggerFactory.getLogger(BankIDFacadeWrapper.class);

    private BankIDProperties bankIDProperties;

    private BIDFacade facade;

    @Autowired
    public BankIDFacadeWrapper(BankIDProperties bankIDProperties) {
        this.bankIDProperties = bankIDProperties;
    }

    private BIDFacade createBankIDFacade() {
        try {
            BIDFactory factory = BIDFactory.getInstance();
            factory.registerBankIDContext(getMerchantConfig());
            BIDFacade minFacade = factory.getFacade(bankIDProperties.getBankIdMerchantName());
            log.info("Intialized BankID Server " + BIDFacade.getVersionInfo());
            return minFacade;
        } catch (BIDException be) {
            int errorCode = be.getErrorCode();
            log.error("errorCode:" + errorCode);
            log.error("Failed to init BankID Server", be);
            return null;
        }
    }

    /**
     * Gets reference to BankID Server API.
     * 
     * @return BankID facade
     */
    BIDFacade getFacade() {
        if (facade == null || bankIDProperties.shouldResetBankId()) {
            log.info("BankIDFacadeWrapper.getFacade: creating BankIDFacade");
            facade = createBankIDFacade();
            bankIDProperties.resetBankIdToFalse();
        }
        return facade;

    }

    void setFacade(BIDFacade facade) {
        this.facade = facade;
    }

    private MerchantConfig getMerchantConfig() {
        MerchantConfig merchantConfig = new MerchantConfig();

        merchantConfig.setGrantedPolicies(bankIDProperties.getBankIdGrantedPolicies());
        merchantConfig.setKeystorePassword(bankIDProperties.getBankIdMerchantKeystorePassword());
        merchantConfig.setMerchantKeystore(bankIDProperties.getBankIdMerchantKeystore());
        merchantConfig.setMerchantName(bankIDProperties.getBankIdMerchantName());
        merchantConfig.setCommTimeout(bankIDProperties.getBankIdTimeout());
        
        if (bankIDProperties.isBankIdLoggingEnabled()) {
            merchantConfig.setLogPropFile(bankIDProperties.getBankIdLoggingPropFile());
            merchantConfig.setLoggerName(bankIDProperties.getBankIdLoggingCategoryName());
        }
        
            
        // This is used in the MITM configuration:
        log.debug("The server hostname is: " + bankIDProperties.getBankIdServerPublicHostname()
                + " and the Public ip is: " + bankIDProperties.getBankIdPublicIP());
        merchantConfig.setWebAddresses(bankIDProperties.getBankIdServerPublicHostname() + ","
                        + bankIDProperties.getBankIdPublicIP());

        return merchantConfig;

    }

}
