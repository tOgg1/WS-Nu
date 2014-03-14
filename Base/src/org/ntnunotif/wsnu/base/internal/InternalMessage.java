package org.ntnunotif.wsnu.base.internal;

import javax.xml.namespace.NamespaceContext;

/**
 * Created by tormod on 3/13/14.
 */
public class InternalMessage {

    public static int STATUS_OK = 0x01;
    public static int STATUS_FAULT = 0x02;
    public static int STATUS_HAS_RETURNING_MESSAGE = 0x04;
    public static int STATUS_RETURNING_MESSAGE_IS_OUTPUTSTREAM = 0x08;
    public static int STATUS_FAULT_UNKNOWN_METHOD = 0x12;
    public static int STATUS_FAULT_INTERNAL_ERROR = 0x22;
    public static int STATUS_FAULT_INVALID_PAYLOAD = 0x42;
    public static int STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM = 0x80;

    public int statusCode;
    private Object message;

    private NamespaceContext context;

    public InternalMessage(int statusCode, Object message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    public void setNamespaceContext(NamespaceContext context) {
        this.context = context;
    }

    public NamespaceContext getNamespaceContext() {
        return context;
    }
}
