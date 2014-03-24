package org.ntnunotif.wsnu.base.util;

import javax.xml.namespace.NamespaceContext;

/**
 * The primary message-object to be sent through the system
 * @author
 * Created by tormod on 3/13/14.
 */
public class InternalMessage {

    public static final int STATUS_OK = 0x01;
    public static final int STATUS_FAULT = 0x02;
    public static final int STATUS_HAS_MESSAGE = 0x04;
    public static final int STATUS_MESSAGE_IS_OUTPUTSTREAM = 0x08;
    public static final int STATUS_FAULT_UNKNOWN_METHOD = 0x10;
    public static final int STATUS_FAULT_INTERNAL_ERROR = 0x20;
    public static final int STATUS_FAULT_INVALID_PAYLOAD = 0x40;
    public static final int STATUS_MESSAGE_IS_INPUTSTREAM = 0x80;
    public static final int STATUS_INVALID_DESTINATION = 0x100;
    public static final int STATUS_ENDPOINTREF_IS_SET = 0x200;

    public int statusCode;
    private Object _message;
    private RequestInformation _requestInformation;

    public InternalMessage(int statusCode, Object message){
        this.statusCode = statusCode;
        this._message = message;
    }

    public InternalMessage(int statusCode, Object message, RequestInformation requestInformation){
        this.statusCode = statusCode;
        this._message = message;
        this._requestInformation = requestInformation;
    }

    public Object getMessage() {
        return _message;
    }

    public void setMessage(Object _message) {
        this._message = _message;
    }

    public RequestInformation getRequestInformation() {
        return _requestInformation;
    }

    public void setRequestInformation(RequestInformation _requestInformation) {
        this._requestInformation = _requestInformation;
    }
}
