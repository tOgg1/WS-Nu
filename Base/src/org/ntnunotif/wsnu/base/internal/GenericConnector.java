package org.ntnunotif.wsnu.base.internal;

import org.oasis_open.docs.wsn.bw_2.NotificationProducer;

import javax.jws.WebMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by tormod on 3/11/14.
 */
public class GenericConnector implements WebServiceConnection{

    private Object _webService;
    private Class _webServiceClass;
    private HashMap<String, Method> _allowedMethods;

    /**
     * Default and only constructor, takes a webService as parameter. Finds all allowed methods.
     */
    public GenericConnector(Object webService) {
        this._webService = webService;
        this._webServiceClass = this._webService.getClass();

        /* Get all methods of this class */
        Method[] methods = this._webServiceClass.getMethods();

        for(Method method : methods){
            Annotation[] annotations = method.getAnnotations();

            /* Check that the method is a @WebMethod, if not, continue*/
            for(Annotation annotation : annotations){
                if(annotation instanceof WebMethod){
                    WebMethod webMethod = (WebMethod)annotation;
                    this._allowedMethods.put(webMethod.operationName(), method);
                    break;
                }else{
                    continue;
                }
            }
            continue;
        }
    }

    @Override
    public Object acceptMessage(Object message) {
        Class objectClass = message.getClass();

        Annotation[] messageAnnotations = objectClass.getAnnotations();

        for(Annotation annotation : messageAnnotations){

        }

        return null;
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
