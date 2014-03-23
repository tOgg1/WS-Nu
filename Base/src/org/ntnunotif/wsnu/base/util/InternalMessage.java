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

    public int _statusCode;
    private Object _message;
    private NamespaceContext _context;
    private EndpointReference _endpointReference;

    public InternalMessage(int statusCode, Object message) {
        this._statusCode = statusCode;
        this._message = message;
    }

    public InternalMessage(int statusCode, Object message, EndpointReference endpointReference){
        this._statusCode = statusCode;
        this._message = message;
        this._endpointReference = endpointReference;
    }

    public void set_endpointReference(EndpointReference _endpointReference) {
        this._endpointReference = _endpointReference;
    }

    public EndpointReference get_endpointReference() {
        return _endpointReference;
    }

    public void set_message(Object message) {
        this._message = message;
    }

    public void set_statusCode(int statusCode) {
        this._statusCode = statusCode;
    }

    public Object get_message() {
        return _message;
    }

    public void setNamespaceContext(NamespaceContext context) {
        this._context = context;
    }

    public NamespaceContext getNamespaceContext() {
        return _context;
    }
}
