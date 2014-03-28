package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.*;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Connector that does the same as UnpackingConnector, but also sends, if possible, the
 * RequestInformation wrapped in the internalmessage sending the message.
 * @author Tormod Haugland
 * Created by tormod on 23.03.14.
 */
public class UnpackingRequestInformationConnector extends WebServiceConnector {

    private Object _webService;
    private Class _webServiceClass;
    private HashMap<String, Method> _allowedMethods;

    public UnpackingRequestInformationConnector(Object webService) {
        super(webService);
        this._webService = webService;
        this._webServiceClass = this._webService.getClass();
        this._allowedMethods = new HashMap<>();

        /* Get all methods of this class */
        Method[] methods = this._webServiceClass.getMethods();

        for(Method method : methods){
            Annotation[] annotations = method.getAnnotations();

            /* Check that the method is a WebMethod, if not, continue*/
            for(Annotation annotation : annotations){
                if(annotation instanceof WebMethod){
                    WebMethod webMethod = (WebMethod)annotation;
                    this._allowedMethods.put(webMethod.operationName(), method);
                    break;
                }else{
                    continue;
                }
            }
        }
    }

    @Override
    public final InternalMessage acceptMessage(InternalMessage internalMessage) {

        Log.d("UnpackingRequestInformationConnector", "Accepting message");

        if(!((internalMessage.statusCode & STATUS_ENDPOINTREF_IS_SET) > 0)){
            Log.d("UnpackingRequestInformationConnector", "EndpointRef not set");
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INTERNAL_ERROR, null);
        }

         /* The message */
        Object potentialEnvelope = internalMessage.getMessage();

        if(!(potentialEnvelope instanceof Envelope)){
            Log.d("UnpackingRequestInformationConnector", "Content not envelope");
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
        }

        /* Unpack the body */
        Envelope envelope = (Envelope)potentialEnvelope;
        Body body = ((Envelope) potentialEnvelope).getBody();

        List<Object> messages = body.getAny();

        RequestInformation requestInformation = internalMessage.getRequestInformation();

        for(Object message : messages){

            /* The class of this message */
            Class objectClass = message.getClass();
            Annotation[] messageAnnotations = objectClass.getAnnotations();

            for(Annotation annotation : messageAnnotations){

                /* Look for the annotation @XmlRootElement */
                if(annotation instanceof XmlRootElement){
                    XmlRootElement xmlRootElement = (XmlRootElement)annotation;
                    Log.d("UnpackingRequestInformationConnector", "Name of annotation: " + xmlRootElement.name());

                    /* Check if this connector's web service has a matching method */
                    if(_allowedMethods.containsKey(xmlRootElement.name())){
                        Method method = _allowedMethods.get(xmlRootElement.name());
                        try {
                            int length = method.getParameterTypes().length;

                            if (length > 2) {
                                Log.e("UnpackingRequestInformationConnector", "Web service at" + _webService + " has a WebMethod " +
                                        "expecting more than 2 arguments. Consider using another connector");
                            }

                            Object[] args = new Object[method.getParameterTypes().length];

                            int webParamIndex = -1, informationIndex = -1;
                            int index = -1;

                            //TODO: Do a more correct search for the correct methods, i.e. take into account that there can be more than one WebParam etc.

                            /* Look for the correct methods through the means of annotation-checking */
                            for (Annotation[] paramAnnotations : method.getParameterAnnotations()) {
                                ++index;
                                for (Annotation paramAnnotation : paramAnnotations) {
                                    if (paramAnnotation instanceof WebParam) {
                                        webParamIndex = index;
                                    } else if (paramAnnotation instanceof Information) {
                                        informationIndex = index;
                                    }
                                }
                            }

                            /* Try and find the web parameter index if the annotation was not found */
                            if (webParamIndex == -1) {
                                index = -1;
                                for (Class<?> paramType : method.getParameterTypes()) {
                                    ++index;
                                    if (message.getClass() == paramType.getClass()) {
                                        webParamIndex = index;
                                        break;
                                    }
                                }
                            }

                            /* Try and find the information paramter index if the annotation was not found */
                            if (informationIndex == -1) {
                                index = -1;
                                for (Class<?> paramType : method.getParameterTypes()) {
                                    ++index;
                                    if (paramType == RequestInformation.class) {
                                        informationIndex = index;
                                        break;
                                    }
                                }
                            }

                            /* If we still can't find the correct parameter, something is wrong */
                            if (webParamIndex == -1) {
                                Log.e("UnpackingRequestInformationConnector", "Invalid Web Service error");
                                throw new InvalidWebServiceException("The index of the web parameter is not found. " +
                                        "If the methodname which is to receive" + message.toString() +
                                        "is different from this object's name, please use a MappedUnpackingConnector");
                            }

                            /* If the information index is STILL not found, try and send it to the index after*/
                            if (informationIndex == -1) {
                                informationIndex = webParamIndex + 1;
                            }

                            InternalMessage returnMessage;

                            args[webParamIndex] = message;
                            args[informationIndex] = requestInformation;

                            Object returnedData = method.invoke(_webService, args);

                            /* If is the case, nothing is being returned */
                            if (method.getReturnType().equals(Void.TYPE)) {
                                returnMessage = new InternalMessage(STATUS_OK, null);
                            } else {
                                returnMessage = new InternalMessage(STATUS_OK | STATUS_HAS_MESSAGE,
                                        returnedData);
                            }
                            return returnMessage;
                        }catch(Exception e) {
                            e.printStackTrace();

                            Log.d("UnpackingRequestInformationConnector", "Some exception occured: " + e.getMessage());
                            //TODO: Add error handling
                            return new InternalMessage(STATUS_FAULT| STATUS_FAULT_INVALID_DESTINATION, null);
                        }
                    }else{
                        Log.d("UnpackingRequestInformationConnector", "Invalid destination");
                        return new InternalMessage(STATUS_FAULT| STATUS_FAULT_INVALID_DESTINATION, null);
                    }
                }
            }

        }
        Log.d("UnpackingRequestInformationConnector", "Unknown method");
        return new InternalMessage(STATUS_FAULT_UNKNOWN_METHOD, null);
    }

    @Override
    public final Class getServiceType() {
        return null;
    }
}
