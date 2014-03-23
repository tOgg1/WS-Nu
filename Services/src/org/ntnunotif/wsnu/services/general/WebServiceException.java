package org.ntnunotif.wsnu.services.general;

/**
 * Created by tormod on 23.03.14.
 */
public class WebServiceException extends RuntimeException {
    public WebServiceException() {
        super();
    }

    public WebServiceException(String message) {
        super(message);
    }
}
