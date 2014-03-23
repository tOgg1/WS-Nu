package org.ntnunotif.wsnu.base.util;

import javax.xml.namespace.NamespaceContext;

/**
 * Created by tormod on 3/13/14.
 */
public class InternalMessage {

    public static final int STATUS_OK = 0x01;
    public static final int STATUS_FAULT = 0x02;
    public static final int STATUS_HAS_RETURNING_MESSAGE = 0x04;
    public static final int STATUS_RETURNING_MESSAGE_IS_OUTPUTSTREAM = 0x08;
    public static final int STATUS_FAULT_UNKNOWN_METHOD = 0x10;
    public static final int STATUS_FAULT_INTERNAL_ERROR = 0x20;
    public static final int STATUS_FAULT_INVALID_PAYLOAD = 0x40;
    public static final int STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM = 0x80;
    public static final int STATUS_INVALID_DESTINATION = 0x100;
    public static final int STATUS_ENDPOINTREF_IS_SET = 0x200;

    public int statusCode;
    private Object _message;
    private NamespaceContext _context;
    @EndpointReference
    private String _endpointReference;

    public InternalMessage(int statusCode, Object message) {
        this.statusCode = statusCode;
        this._message = message;
    }

    public InternalMessage(int statusCode, Object message, String endpointReference){
        this.statusCode = statusCode;
        this._message = message;
        this._endpointReference = endpointReference;
    }

    public void setEndpointReference(String endpointReference) {
        this._endpointReference = endpointReference;
    }

    public String getEndpointReference() {
        return _endpointReference;
    }

    public void setMessage(Object message) {
        this._message = message;
    }

    public Object getMessage() {
        return _message;
    }

    public void setNamespaceContext(NamespaceContext context) {
        this._context = context;
    }

    public NamespaceContext getNamespaceContext() {
        return _context;
    }
}
