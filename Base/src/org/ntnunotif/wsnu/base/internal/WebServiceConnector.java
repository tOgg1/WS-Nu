package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.EndpointReference;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.InvalidWebServiceException;
import org.ntnunotif.wsnu.base.util.Log;

import javax.jws.WebService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author Tormod Haugland
 * Created by tormod on 3/3/14.
 */
public abstract class WebServiceConnector implements ServiceConnection{

    /**
     * EndpointReference of this web service connection
     */
    @EndpointReference(type="uri")
    public String endpointReference;

    public static int webServiceCount = 0;

    protected WebServiceConnector(final Object webService){
        Annotation[] annotations = webService.getClass().getAnnotations();
        boolean isWebService = false, referenceIsSet = false;
        for (Annotation annotation : annotations) {
            if(annotation instanceof WebService){
                isWebService = true;
            }
        }

        if(!isWebService){
            Log.e("WebServiceConnector", "WebService annotation not set for object" + webService);
            throw new InvalidWebServiceException("Object passed in to WebServiceConnector does not carry the Webservice annotation");
        }

        /* look for endpointReference */
        Field[] fields = webService.getClass().getFields();
        Annotation[] fieldAnnotations;

        for (Field field : fields) {

            if(field.getType() != String.class){
                continue;
            }

            fieldAnnotations = field.getDeclaredAnnotations();
           for(Annotation annotation : fieldAnnotations){
                if(annotation instanceof EndpointReference){
                    try {
                        endpointReference = (String)field.get(webService);
                        referenceIsSet = true;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        break;
                    }
                }
           }
        }
    }
}
