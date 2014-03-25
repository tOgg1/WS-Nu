package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.*;
import org.trmd.ntsh.NothingToSeeHere;

import javax.jws.WebService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * The generic WebServiceConnector used by default by WS-Nu. Implements basic @WebService annotation checking for passed-in objects.
 * Also generates automatic endpointReferences if t
 * @author Tormod Haugland
*          Created by tormod on 3/3/14.
 */
public abstract class WebServiceConnector implements ServiceConnection{

    /**
     * EndpointReference of this web service connection
     */
    @EndpointReference(type="uri")
    public String _endpointReference;

    public static int _webServiceCount = 0;

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
        List<Field> fields = (List<Field>)Utilities.getFieldsUpTo(webService.getClass(), null);

        Annotation[] fieldAnnotations;

        for (Field field : fields) {

            if(field.getType() != String.class){
                continue;
            }

            fieldAnnotations = field.getDeclaredAnnotations();
            for(Annotation annotation : fieldAnnotations){
                if(annotation instanceof EndpointReference){
                    try {
                        field.setAccessible(true);
                        _endpointReference = (String)field.get(webService);
                        referenceIsSet = true;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }

        if(!referenceIsSet){
            Log.w("WebServiceConnector", "EndpointReference is not set in the Web Service " + webService + ". Please considering adding" +
                    "a String-field with the annotation @EndpointReference in your Web Service");
            _endpointReference = webService.getClass().getSimpleName() + NothingToSeeHere.t("000"+ _webServiceCount);
        }
    }
}
