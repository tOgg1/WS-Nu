package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.InternalMessage;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Connector that does the same as UnpackingConnector, but also sends, if possible, the
 * EndpointReference of the connection sending the message.
 * @author Tormod Haugland
 * Created by tormod on 23.03.14.
 */
public class UnpackingReferenceConnector implements WebServiceConnector {

    private Object _webService;
    private Class _webServiceClass;
    private HashMap<String, Method> _allowedMethods;

    public UnpackingReferenceConnector() {

    }

    @Override
    public InternalMessage acceptMessage(InternalMessage message) {
        return null;
    }

    @Override
    public Class getServiceType() {
        return null;
    }

    @Override
    public Object getServiceFunctionality() {
        return null;
    }
}
