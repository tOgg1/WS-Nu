//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

package org.ntnunotif.wsnu.base.util;

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
    public static final int STATUS_FAULT_INVALID_DESTINATION = 0x80;
    public static final int STATUS_FAULT_NOT_SUPPORTED = 0x100;
    public static final int STATUS_FAULT_NOT_FOUND = 0x200;
    public static final int STATUS_FAULT_ACCESS_NOT_ALLOWED = 0x400;
    public static final int STATUS_MESSAGE_IS_INPUTSTREAM = 0x800;
    public static final int STATUS_ENDPOINTREF_IS_SET = 0x1000;
    public static final int STATUS_EXCEPTION_SHOULD_BE_HANDLED = 0x2000;
    public static final int STATUS_MESSAGE_IS_SOAPENVELOPE = 0x4000;
    public static final int STATUS_MESSAGE_IS_STRING = 0x8000;

    /**
     * The status code of the InternalMessage. Stored as a 32-bit flag.
     */
    public int statusCode;

    /**
     * The actual message paylod
     */
    private Object _message;

    /**
     * The requestinformation. This will in most scenarios only hold information for messages going up the stack.
     */
    private RequestInformation _requestInformation;

    /**
     * Constructor taking a status code and message.
     * @param statusCode
     * @param message
     */
    public InternalMessage(int statusCode, Object message){
        this.statusCode = statusCode;
        this._message = message;
        _requestInformation = new RequestInformation();
    }

    /**
     * Constructor taking a status code, message and requestInformation
     * @param statusCode
     * @param message
     * @param requestInformation
     */
    public InternalMessage(int statusCode, Object message, RequestInformation requestInformation){
        this.statusCode = statusCode;
        this._message = message;
        this._requestInformation = requestInformation;
    }

    /**
     * Retrieves the message of the InternalMessage.
     * @return
     */
    public Object getMessage() {
        return _message;
    }

    /**
     * Sets the message of the InternalMessage.
     * @param message
     */
    public void setMessage(Object message) {
        this._message = message;
    }

    /**
     * Retrieves the request information of the InternalMessage.
     * @return
     */
    public RequestInformation getRequestInformation() {
        return _requestInformation;
    }

    /**
     * Sets the request information of the InternalMessage.
     * @param _requestInformation
     */
    public void setRequestInformation(RequestInformation _requestInformation) {
        this._requestInformation = _requestInformation;
    }

    @Override
    public String toString() {
        return "InternalMessage{" +
                "statusCode=" + statusCode +
                ", _message=" + _message +
                ", _requestInformation=" + _requestInformation +
                '}';
    }
}
