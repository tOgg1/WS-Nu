package org.ntnunotif.wsnu.base.internal;

/**
 * Created by tormod on 3/13/14.
 */
public class InternalMessage {

    public int statusCode;
    private Object message;

    public InternalMessage(int statusCode, Object message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }
}
