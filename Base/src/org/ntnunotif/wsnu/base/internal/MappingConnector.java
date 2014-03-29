package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.InvalidWebServiceException;
import org.ntnunotif.wsnu.base.util.Log;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Created by tormod on 25.03.14.
 */
public class MappingConnector extends WebServiceConnector{

    private final Map<String, String> _methodMap;
    private final Object _webService;
    private final Map<String, Method> _allowedMethods;

    /**
     *
     * @param webService
     * @param methodMap Takes the relationship between method-names and (@WebParam) parameter names. They have to be specified in the order
     *                  Entry<ParameterType, MethodName>
     */
    public MappingConnector(final Object webService, final Map<String, String> methodMap) {


        super(webService);
        ArrayList<String> methodNames = new ArrayList<>();
        _allowedMethods = new HashMap<String, Method>();

        for(Method method : webService.getClass().getMethods()){
            _allowedMethods.put(method.getName(), method);
            methodNames.add(method.getName());
        }

        /* Run a quick check to see if all methods-name relations are well-defined, i.e. unique and the methodnames exist*/
        for(Map.Entry<String, String> methodMapEntry : methodMap.entrySet()) {
            String methodName = methodMapEntry.getValue();

            if(!methodNames.contains(methodName)){
                throw new InvalidWebServiceException("Hashmap passed in has a methodName not supported by the passedin webservice object");
            }
        }

        this._webService = webService;
        this._methodMap = methodMap;
    }

    @Override
    public final InternalMessage acceptMessage(InternalMessage internalMessage) {

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

        for(Object message : messages){
            Class objectClass = message.getClass();

            if(!(_methodMap.containsKey(objectClass.getSimpleName()))){
                Log.e("MappingConnector", "Invalid message-object passed in");
                return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
            }

            Method method = _allowedMethods.get(_methodMap.get(objectClass.getSimpleName()));
            Object[] args = new Object[method.getParameterTypes().length];

            /* Find the argument-index of the message. Will use the first found */
            int index = -1;
            for (Class<?> aClass : method.getParameterTypes()) {
                ++index;
                if(aClass == objectClass){
                    break;
                 }
            }

            if(index == -1){
                Log.e("MappingConnector", "Index of argument" + objectClass + " not found in method" + method.getName());
                return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
            }

            args[index] = message;

            try {
                Object returnedData = method.invoke(_webService, args);

                 /* If is the case, nothing is being returned */
                if (method.getReturnType().equals(Void.TYPE)) {
                    return new InternalMessage(STATUS_OK, null);
                } else {
                    return new InternalMessage(STATUS_OK | STATUS_HAS_MESSAGE, returnedData);
                }

            }catch(IllegalAccessException e){
                Log.e("UnpackingRequestInformationConnector","The method being accessed is not public. Something must be wrong with the" +
                        "org.generated classes.\n A @WebMethod can not have private access");
                return new InternalMessage(STATUS_FAULT| STATUS_FAULT_INTERNAL_ERROR, null);
            }catch(InvocationTargetException e){
                return new InternalMessage(STATUS_FAULT|STATUS_EXCEPTION_SHOULD_BE_HANDLED, e.getTargetException());
            }
        }

        return new InternalMessage(STATUS_FAULT, null);
    }

    @Override
    public final Class getServiceType() {
        return null;
    }

}
