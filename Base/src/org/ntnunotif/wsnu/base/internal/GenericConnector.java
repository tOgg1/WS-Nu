package org.ntnunotif.wsnu.base.internal;

import org.oasis_open.docs.wsn.bw_2.NotificationProducer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by tormod on 3/11/14.
 */
public class GenericConnector implements WebServiceConnection{

    private Object _webService;
    private Method[] _allowedMethods;

    /**
     * Default constructor
     */
    public GenericConnector(Object webService) {
        this._webService = null;


    }

    @Override
    public void acceptMessage(Object message) {

    }

    @Override
    public Class getServiceType() {
        return _webService.getClass();
    }

    @Override
    public Method[] getServiceFunctionality() {
        return _allowedMethods;
    }

}
