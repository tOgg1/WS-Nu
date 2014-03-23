package org.ntnunotif.wsnu.base.internal;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Connector that takes soap-envelopes and sends them forward. A connected web service MUST implement the WebService
 * interface defined in the services/ package. Or implement another method that has the annotation @WebMethod(operationName = "acceptSoapMessage()"
 * Created by tormod on 23.03.14.
 */
public class SoapForwardConnector implements WebServiceConnector {

    private Object _webService;
    private Class _webServiceClass;

    public SoapForwardConnector(Object webService) {
        _webServiceClass = webService.getClass();

    }

    @Override
    public InternalMessage acceptMessage(InternalMessage message) {
        return null;
    }

    @Override
    public Class getServiceType() {
        return _webServiceClass;
    }

    @Override
    public Object getServiceFunctionality() {
        return null;
    }
}
