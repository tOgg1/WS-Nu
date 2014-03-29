package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.*;
import org.trmd.ntsh.NothingToSeeHere;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * The generic WebServiceConnector used by default by WS-Nu. Implements basic @WebService annotation checking for passed-in objects.
 * Also generates automatic endpointReferences if no such reference exists in the web service object.
 * @author Tormod Haugland
*          Created by tormod on 3/3/14.
 */
public abstract class WebServiceConnector implements ServiceConnection{

    final Object _webService;

    /**
     * EndpointReference of this web service connection
     */
    @EndpointReference(type="uri")
    public String _endpointReference;

    /**
     * The total amount of WebServiceConnector's in existance.
     */
    public static int _webServiceCount = 0;

    /**
     *
     */
    private RequestInformation _requestInformation;

    private Method _requestMethod;

    /**
     * The default constructor. Looks for the {@link WebService} for the passed in WebService object and the {@link org.ntnunotif.wsnu.base.util.EndpointReference}
     * annotation for the endpointreference. Also looks for a field with the annotation {@link org.ntnunotif.wsnu.base.util.Connection} to set a reference to this connector. Will also look for a method to send plain requests to, by looking for the {@link javax.jws.WebMethod} annotation with operationName=AcceptRequest
     * @param webService
     */
    protected WebServiceConnector(final Object webService){
        _webService = webService;

        Annotation[] annotations = webService.getClass().getAnnotations();
        boolean isWebService = false, referenceIsSet = false, connectionIsSet = false;
        for (Annotation annotation : annotations) {
            if(annotation instanceof WebService){
                isWebService = true;
            }
        }

        if(!isWebService){
            Log.e("WebServiceConnector", "WebService annotation not set for object" + webService);
            throw new InvalidWebServiceException("Object passed in to WebServiceConnector does not carry the Webservice annotation");
        }

        /* Look for endpointReference */
        List<Field> fields = (List<Field>)Utilities.getFieldsUpTo(webService.getClass(), null);

        Annotation[] fieldAnnotations;

        for (Field field : fields) {
            fieldAnnotations = field.getDeclaredAnnotations();
            for(Annotation annotation : fieldAnnotations){
                if(field.getClass().equals(String.class)){
                    break;
                }
                if(annotation instanceof EndpointReference) {
                    try {
                        field.setAccessible(true);
                        _endpointReference = (String) field.get(webService);
                        if (_endpointReference == null) {
                            try {
                                _endpointReference = ApplicationServer.getURI() + "/" + webService.getClass().getSimpleName().toLowerCase() + "_" + NothingToSeeHere.t("000" + _webServiceCount++);
                            } catch (Exception e) {
                                Log.e("WebServiceConnector", "Fetching the application server's URI failed. This is probably" +
                                        "due to an instantiation error. Please consider instantiating the ApplicationServer before" +
                                        "creating a connector");
                                return;
                            }
                            Log.w("WebServiceConnector", "The endpointreference was found, but carries no information (i.e. is null). " +
                                    "\nConsider setting the endpoint in the constructor or by other means before assigning it to a connector." +
                                    "\nThe reference \n\t" + _endpointReference + "\nhas been randomly generated");
                        }
                        field.set(webService, _endpointReference);
                        referenceIsSet = true;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }

            for(Annotation annotation : fieldAnnotations){
                if(annotation instanceof Connection){
                    try{
                        field.setAccessible(true);
                        field.set(webService, this);
                        connectionIsSet = true;
                    } catch (IllegalAccessException e) {
                        Log.d("WebServiceConnector", "Setting the field with annotation Connection failed.");
                        continue;
                    }
                }
            }
        }

        if(!referenceIsSet){
            Log.w("WebServiceConnector", "EndpointReference is not set in the Web Service " + webService + ". Please consider adding" +
                    "a String-field with the annotation @EndpointReference in your Web Service");
            try {
                _endpointReference = ApplicationServer.getURI() +"/"+ webService.getClass().getSimpleName().toLowerCase() +"_"+ NothingToSeeHere.t("000"+ _webServiceCount++);
            } catch (Exception e) {
                Log.e("WebServiceConnector", "Fetching the application server's URI failed. This is probably" +
                        "due to an instantiation error. Please consider instantiating the ApplicationServer before" +
                        "creating a connector");
                return;
            }
        }

        if(!connectionIsSet){
            Log.w("WebServiceConnector", "Connection is not set in the Web Service " + webService + ". Please considering adding" +
                   "this WebService Connector" + this + " manually to the Web Service in some variable");
        }

        /* Look for requestMethod */
        Method[] methods = webService.getClass().getMethods();

        outer:
        for(Method method : methods){
            Annotation[] methodAnnotations = method.getDeclaredAnnotations();
            for (Annotation annotation : methodAnnotations){
                if(annotation instanceof WebMethod){
                    if(((WebMethod)annotation).operationName().equals("AcceptRequest")){
                        _requestMethod = method;
                        break outer;
                    }
                }
            }
        }
    }

    /**
     * Sets the method that are to accept requests (i.e requests <b>without</b> content )
     * @param requestMethod
     */
    public void setRequestMethod(Method requestMethod) {
        _requestMethod = requestMethod;
    }

    @Override
    public InternalMessage acceptRequest(InternalMessage message) {
        Log.d("WebServiceConnector", "Accepted requestMessage");
        if(_requestMethod == null){
            Log.e("WebServiceConnector", "AcceptRequest function called on a connector not having defined the requestmethod. " +
                    "Please call setRequestMethod with the appropriate method");
            return new InternalMessage(STATUS_FAULT| STATUS_FAULT_NOT_SUPPORTED, null);
        }else{
            try {
                Log.d("WebServiceConnector", "Forwarding requestMessage");
                _requestMethod.setAccessible(true);
                Object returnedData = _requestMethod.invoke(_webService);

                if(_requestMethod.getReturnType().equals(Void.TYPE)) {
                    return new InternalMessage(STATUS_OK, null);
                }else if(_requestMethod.getReturnType().equals(InternalMessage.class)){
                    return (InternalMessage) returnedData;
                }else{
                    return new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, returnedData);
                }
            } catch (IllegalAccessException e){
                Log.e("WebServiceConnector", "AcceptRequest-method of the web service is inaccessible, even after setAccessible is called.");
                return new InternalMessage(STATUS_FAULT| STATUS_FAULT_INTERNAL_ERROR, null);
            } catch (InvocationTargetException e) {
                return new InternalMessage(STATUS_FAULT | STATUS_FAULT_INVALID_DESTINATION, null);
            }
        }
    }

    @Override
    public void endpointUpdated(String newEndpointReference) {
        this._endpointReference = newEndpointReference;
    }

    @Override
    public String getServiceEndpoint() {
        return this._endpointReference;
    }

    @Override
    public Class<?> getServiceType() {
        return _webService.getClass();
    }

    @Override
    public RequestInformation getReqeustInformation() {
        return _requestInformation;
    }
}
