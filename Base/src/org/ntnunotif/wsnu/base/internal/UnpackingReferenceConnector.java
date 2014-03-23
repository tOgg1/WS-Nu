package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.EndpointParam;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.InvalidWebServiceException;
import org.ntnunotif.wsnu.base.util.Log;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.jws.WebMethod;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

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

    public UnpackingReferenceConnector(Object webService) {
        this._webService = webService;
        this._webServiceClass = this._webService.getClass();
        this._allowedMethods = new HashMap<>();

        /* Get all methods of this class */
        Method[] methods = this._webServiceClass.getMethods();

        for(Method method : methods){
            Annotation[] annotations = method.getAnnotations();

            Log.d("UnpackingReferenceConnector", "Method: " + method.getName());

            /* Check that the method is a @WebMethod, if not, continue*/
            for(Annotation annotation : annotations){
                if(annotation instanceof WebMethod){
                    WebMethod webMethod = (WebMethod)annotation;
                    Log.d("UnpackingReferenceConnector", "Allowedmethod added: " + webMethod.operationName());
                    this._allowedMethods.put(webMethod.operationName(), method);
                    break;
                }else{
                    continue;
                }
            }
        }
    }

    @Override
    public InternalMessage acceptMessage(InternalMessage internalMessage) {

        if(!((internalMessage.statusCode & STATUS_ENDPOINTREF_IS_SET) > 0)){
            Log.d("UnpackingReferenceConnector", "EndpointRef not set");
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INTERNAL_ERROR, null);
        }

         /* The message */
        Object potentialEnvelope = internalMessage.getMessage();

        if(!(potentialEnvelope instanceof Envelope)){
            Log.d("UnpackingReferenceConnector", "Content not envelope");
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
        }

        /* Unpack the body */
        Envelope envelope = (Envelope)potentialEnvelope;
        Body body = ((Envelope) potentialEnvelope).getBody();

        List<Object> messages = body.getAny();

        for(Object message : messages){

            /* The class of this message */
            Class objectClass = message.getClass();
            Annotation[] messageAnnotations = objectClass.getAnnotations();

            for(Annotation annotation : messageAnnotations){

                /* Look for the annotation @XmlRootElement */
                if(annotation instanceof XmlRootElement){
                    XmlRootElement xmlRootElement = (XmlRootElement)annotation;
                    Log.d("UnpackingReferenceConnector", "Name of annotation: " + xmlRootElement.name());
                    /* Check if this connector's web service has a matching method */
                    if(_allowedMethods.containsKey(xmlRootElement.name())){
                        Method method = _allowedMethods.get(xmlRootElement.name());
                        try {

                            if(method.getParameterTypes().length > 2){
                                Log.e("UnpackingReferenceConnector", "Method error");
                                throw new InvalidWebServiceException("Web service at" + _webService + " has a WebMethod " +
                                                                     "expecting more than 2 arguments, which is not supported by this Connector." +
                                                                     "Consider using ");
                            /* We are here looking for a parameter with the annotation EndpointParam */
                            }else if(method.getParameterTypes().length > 1){
                                Annotation[][] paramAnnotations = method.getParameterAnnotations();

                                /* Is the endpointparam first or last?*/
                                int indexOfEndPointParam = -1;

                                /* For each parameter */
                                for (Annotation[] paramAnnotation : paramAnnotations) {
                                    ++indexOfEndPointParam;

                                    /* For each annotation */
                                    for (Annotation annotation1 : paramAnnotation) {
                                        if(annotation1 instanceof EndpointParam){

                                            /* Run method on the Web Service */
                                            InternalMessage returnMessage;
                                            Object method_returnedData;

                                            if(indexOfEndPointParam == 0){
                                               method_returnedData = method.invoke(_webService, internalMessage.getEndpointReference(), message);
                                            }else{
                                                method_returnedData = method.invoke(_webService, message, internalMessage.getEndpointReference());
                                            }

                                            /* If is the case, nothing is being returned */
                                            if(method.getReturnType().equals(Void.TYPE)){
                                                returnMessage = new InternalMessage(STATUS_OK, null);
                                            }else{
                                                returnMessage = new InternalMessage(STATUS_OK|STATUS_HAS_RETURNING_MESSAGE,
                                                        method_returnedData);
                                            }
                                            return returnMessage;

                                        }
                                    }
                                }
                                throw new InvalidWebServiceException("Web service at " + _webService + " has a WebMethod " +
                                                                     "with two methods, but neither of them has an EndpointParam annotation. " +
                                                                     "This is not supported by this Connector");
                            }
                            else{
                                /* Run method on the Web Service */
                                InternalMessage returnMessage;
                                Object method_returnedData = method.invoke(_webService, message);

                            /* If is the case, nothing is being returned */
                                if(method.getReturnType().equals(Void.TYPE)){
                                    returnMessage = new InternalMessage(STATUS_OK, null);
                                }else{
                                    returnMessage = new InternalMessage(STATUS_OK|STATUS_HAS_RETURNING_MESSAGE,
                                            method_returnedData);
                                }
                                return returnMessage;
                            }


                        } catch (IllegalAccessException e) {
                            Log.e("Unpacking Connector", "The method being accessed is not public. Something must be wrong with the" +
                                    "generated classes.\n A @WebMethod can not have private access");
                            e.printStackTrace();
                            return null;
                        } catch (InvocationTargetException e) {
                            Log.e("Unpacking Connector", "The method being accessed are being feeded an invalid amount of " +
                                    "parameters, or something even more obscure has occured.");
                        }
                    }else{
                        Log.d("UnpackingReferenceConnector", "Invalid destination");
                        return new InternalMessage(InternalMessage.STATUS_INVALID_DESTINATION, null);
                    }
                }
            }

        }
        Log.d("UnpackingReferenceConnector", "Unknown method");
        return new InternalMessage(InternalMessage.STATUS_FAULT_UNKNOWN_METHOD, null);
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
