package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.InvalidWebServiceException;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.jws.WebMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Connector that takes soap-envelopes and sends them forward. A connected web service MUST implement the WebService
 * interface defined in the services/ package. Or implement another method that has the annotation @WebMethod(operationName = "acceptSoapMessage())"
 * If the connected Web Service has more than one such method, the first found will be selected. If you need two invocations, you should implement a new version of WebServiceConnector
 * Created by tormod on 23.03.14.
 */
public class SoapForwardConnector extends WebServiceConnector {

    private final Object _webService;
    private final Class _webServiceClass;
    private final Method _soapMethod;

    public SoapForwardConnector(final Object webService) {
        super(webService);
        _webServiceClass = webService.getClass();
        _webService = webService;

        Method[] methods = webService.getClass().getMethods();

        for(Method method : methods) {
            Annotation[] annotations = method.getAnnotations();

            for(Annotation annotation : annotations) {
                if(annotation instanceof WebMethod){
                    if(((WebMethod)annotation).operationName().equals("acceptSoapMessage")) {
                        _soapMethod = method;
                        return;
                    }else{
                        continue;
                    }
                }
            }
        }
        throw new InvalidWebServiceException("[SoapForwardConnector]: Invalid webService object passed. Must implement some function having the annotation @WebMethod(operationName = \"acceptSoapMessage()");
    }

    @Override
    public final InternalMessage acceptMessage(InternalMessage message) {
        Object messageContent = message.getMessage();

        if(!(messageContent instanceof Envelope)){
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
        }

        Envelope soapEnvelope = (Envelope)message.getMessage();

        try {
            Object method_returnedData = _soapMethod.invoke(_webService, soapEnvelope);

            /* If is the case, nothing is being returned */
            if(_soapMethod.getReturnType().equals(Void.TYPE)){
                return new InternalMessage(STATUS_OK, null);
            }else{
                return new InternalMessage(STATUS_OK| STATUS_HAS_MESSAGE,
                        method_returnedData);
            }
        } catch (IllegalAccessException e) {
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
        } catch (InvocationTargetException e) {
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
        }
    }

    @Override
    public final Class getServiceType() {
        return _webServiceClass;
    }

    @Override
    public final Method getServiceFunctionality() {
        return _soapMethod;
    }
}
