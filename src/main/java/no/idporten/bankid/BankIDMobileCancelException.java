package no.idporten.bankid;

import javax.security.auth.login.LoginException;

/**
 * Exception thrown when user cancels authentication.
 */
public class BankIDMobileCancelException extends LoginException {

    public BankIDMobileCancelException() {
    }

    public BankIDMobileCancelException(String msg) {
        super(msg);
    }

}
