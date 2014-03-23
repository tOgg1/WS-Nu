package org.ntnunotif.wsnu.base.internal;

import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.jws.WebMethod;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import static org.ntnunotif.wsnu.base.internal.InternalMessage.*;

/**
 * Connector that takes a soap-envelope, unpacks it's body, and sends it forward.
 * This does function DOES not bother with checking the soap-headers for any information.
 * @author Tormod Haugland
 * Created by tormod on 3/11/14.
 */
public class UnpackingConnector implements WebServiceConnector {

    private Object _webService;
    private Class _webServiceClass;
    private HashMap<String, Method> _allowedMethods;

    /**
     * Default and only constructor, takes a webService as parameter. Finds all allowed methods.
     */
    public UnpackingConnector(Object webService) {
        this._webService = webService;
        this._webServiceClass = this._webService.getClass();
        this._allowedMethods = new HashMap<>();

        /* Get all methods of this class */
        Method[] methods = this._webServiceClass.getMethods();

        for(Method method : methods){
            Annotation[] annotations = method.getAnnotations();

            /* Check that the method is a @WebMethod, if not, continue*/
            for(Annotation annotation : annotations){
                System.out.println(annotation.getClass());
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

    //TODO: ContextHandling
    @Override
    //TODO: Support multiple messages
    public InternalMessage acceptMessage(InternalMessage internalMessage) {

        /* The message */
        Object potentialEnvelope = internalMessage.getMessage();

        if(!(potentialEnvelope instanceof Envelope)){
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
                    System.out.println(xmlRootElement.name());
                    /* Check if this connector's web service has a matching method */
                    if(_allowedMethods.containsKey(xmlRootElement.name())){
                        Method method = _allowedMethods.get(xmlRootElement.name());
                        try {
                            /* Run method on the Web Service */
                            InternalMessage returnMessage;
                            Object method_returnedData = method.invoke(_webService, message);

                            /* If is the case, nothing is being returned */
                            if(method.getReturnType().equals(Void.TYPE)){
                                System.out.println("Heeeey!");;
                                returnMessage = new InternalMessage(STATUS_OK, null);
                            }else{
                                System.out.println("Hey!");
                                returnMessage = new InternalMessage(STATUS_OK|STATUS_HAS_RETURNING_MESSAGE,
                                                                    method_returnedData);
                            }
                            return returnMessage;
                        } catch (IllegalAccessException e) {
                            System.err.println("The method being accessed is not public. Something must be wrong with the" +
                                               "generated classes.\n A @WebMethod can not have private access");
                            e.printStackTrace();
                            return null;
                        } catch (InvocationTargetException e) {
                            System.err.println("The method being accessed are being feeded an invalid amount of " +
                                               "parameters, or something even more obscure has occured.");
                        }
                    }else{
                        System.out.println("Invalid Dest :/");

                        return new InternalMessage(InternalMessage.STATUS_INVALID_DESTINATION, null);
                    }
                }
            }

        }
        return new InternalMessage(InternalMessage.STATUS_FAULT_UNKNOWN_METHOD, null);
    }

    @Override
    public Class getServiceType() {
        return _webService.getClass();
    }

    @Override
    public HashMap<String, Method> getServiceFunctionality() {
        return _allowedMethods;
    }

}
