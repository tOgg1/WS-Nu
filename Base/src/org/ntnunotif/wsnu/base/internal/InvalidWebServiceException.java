package org.ntnunotif.wsnu.base.internal;

/**
 * Created by tormod on 23.03.14.
 */
public class InvalidWebServiceException extends RuntimeException {

    public InvalidWebServiceException() {
        super();
    }

    public InvalidWebServiceException(String message) {
        super(message);
    }

    public InvalidWebServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidWebServiceException(Throwable cause) {
        super(cause);
    }

    protected InvalidWebServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
