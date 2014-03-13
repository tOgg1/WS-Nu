package org.ntnunotif.wsnu.base.internal;

import org.oasis_open.docs.wsn.bw_2.NotificationProducer;

import javax.jws.WebMethod;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
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
            continue;
        }

    }

    @Override
    public Object acceptMessage(Object message) {
        Class objectClass = message.getClass();

        Annotation[] messageAnnotations = objectClass.getAnnotations();

        for(Annotation annotation : messageAnnotations){

            /* Look for the annotation @XmlRootElement */
            if(annotation instanceof XmlRootElement){
                XmlRootElement xmlRootElement = (XmlRootElement)annotation;

                /* Check if this connector's web service has a matching method */
                if(_allowedMethods.containsKey(xmlRootElement.name())){
                    Method method = _allowedMethods.get(xmlRootElement.name());
                    try {
                        /* Run method on the Web Service */
                        return method.invoke(_webService, message);
                    } catch (IllegalAccessException e) {
                        System.err.println("The method being accessed is not public. Something must be wrong with the" +
                                           "generated classes.\n A @WebMethod can not have private access");
                        e.printStackTrace();
                        return null;
                    } catch (InvocationTargetException e) {
                        System.err.println("The method beinc accessed are being feeded an invalid amount of " +
                                           "parameters, or something even more obscure has occured.");
                    }
                }else{
                    return null;
                }
            }
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
