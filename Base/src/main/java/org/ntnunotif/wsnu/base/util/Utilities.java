package org.ntnunotif.wsnu.base.util;

import com.google.common.collect.Lists;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.xmlsoap.schemas.soap.envelope.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.WebFault;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Utility function for the Base module.
 * @author Tormod Haugland
 * Created by tormod on 3/3/14.
 */
public class Utilities {

    /**
     * Takes an outputstream with data. Then prepares an inputstream with this data and returns it.
     * @param stream
     * @return
     */
    public static InputStream convertToInputStream(OutputStream stream){
        return null;
    }


    /**
     * Takes any object, and tries to convert its data to an inputstream. The internal attempt-order is as follows:
     * 1.   String
     * 2.   Something parseable
     * 3.   Inputstream
     * 4.   Outputstream
     * 5.   Byte-array
     * 6.   Byte-buffer
     * 7.   Char-array
     * 8.   Char-buffer
     * 9.   Int-array
     * 10.  Int-buffer
     * 11.  Float-array
     * 12.  Float-buffer
     * 13.  List
     * 14.  Set
     * @param message
     * @return
     */
    //TODO: Implement everything
    //TODO: Remove, this is not used?
    public static InputStream convertUnknownToInputStream(Object message) {

        /* Are we dealing with a string?*/
        if(message instanceof String){
            try {
                return new ByteArrayInputStream(((String) message).getBytes("UTF_8"));
            } catch (UnsupportedEncodingException e) {
                return new ByteArrayInputStream(((String)message).getBytes());
            }
        }

        /* Try and parse it with the XMLParser*/
        try{
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            XMLParser.writeObjectToStream(message, stream);
            byte[] bytes = stream.toByteArray();
            return new ByteArrayInputStream(bytes);

        }
        catch(JAXBException e){
            e.printStackTrace();
        }

        /* A byte-array? */
        if(message instanceof byte[]){
            return new ByteArrayInputStream((byte[]) message);
        }

        /* Byte-buffer? */
        if(message instanceof ByteBuffer){
            byte[] bytes = ((ByteBuffer) message).array();
            return new ByteArrayInputStream(bytes);
        }
        return null;
    }

    public static InputStream convertParseableToInputStream(Object message) throws JAXBException{
        /* Try and parse it with the XMLParser*/
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLParser.writeObjectToStream(message, stream);
        return convertToByteArrayInputStream(stream);
    }

    public static InputStream convertToByteArrayInputStream(ByteArrayOutputStream stream){
        byte[] bytes = stream.toByteArray();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Takes in a class and checks if it has the passed in method.
     * @return
     */
    public static boolean hasMethod(Class<?> c, Method method){
        for (Method method1 : c.getMethods()) {
            if(method1.equals(method))
                return true;
        }
        return false;
    }




    /**
     * Takes in a class and checks if it has a method with the passed in name.
     */
    public static boolean hasMethodWithName(Class<?> c, String methodName){
        for(Method method : c.getMethods()) {
            if(method.getName().equals(methodName)){
                return true;
            }
        }
        return false;
    }

    /**
     * Takes in a regex-string and checks if the passed in class as a method matching the regex.
     */
    public static boolean hasMethodWithRegex(Class<?> c, String regex){
        for(Method method : c.getMethods()){
            if(method.getName().matches(regex)){
                return true;
            }
        }
        return false;
    }

    /**
     * Takes in a regex-string and returns a method if the class has a method matching the regex. Will
     * return the first method encountered. Note that the entire methodname has to match the regex. Not just parts of
     * it.
     * @param c
     * @param regex
     * @return
     */
    public static Method getMethodByRegex(Class<?> c, String regex){
        for (Method method : c.getMethods()) {
            if(method.getName().matches(regex)){
                return method;
            }
        }
        return null;
    }

    /**
     * Takes in a methodname and returns a method if a method in the passed-in class matches the methodname.
     * @param c The relevant class.
     * @param methodName The methodname that is to be searched for.
     * @return
     */
    public static Method getMethodByName(Class<?> c, String methodName){
        for (Method method : c.getMethods()) {
            if(method.getName().equals(methodName)){
                return method;
            }
        }
        return null;
    }

    /**
     * Finds an annotation of the class aClass by looking for an annotation matching the class annotationClass
     * @param aClass
     * @param annotationClass
     * @return
     */
    public static Annotation findAnnotation(Class<?> aClass, Class<? extends Annotation> annotationClass){
        for (Annotation annotation : aClass.getAnnotations()) {
            if(annotation.annotationType().equals(annotationClass)){
                return annotation;
            }
        }
        return null;
    }

    public static Annotation findAnnotation(Object object, Class<? extends Annotation> annotationClass){
        return findAnnotation(object.getClass(), annotationClass);
    }

    /**
     * Function to get ALL fiels up the class hierarcy.
     * Credit: John B @ stackoverflow: http://stackoverflow.com/questions/16966629/what-is-the-difference-between-getfields-getdeclaredfields-in-java-reflection
     * @param startClass Class to get all methods for
     * @param exclusiveParent Any parentclass that is to be excluded from the retrieval.
     * @return
     */
    public static Iterable<Field> getFieldsUpTo(@Nonnull Class<?> startClass,
                                                @Nullable Class<?> exclusiveParent) {

        List<Field> currentClassFields = Lists.newArrayList(startClass.getDeclaredFields());
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
            List<Field> parentClassFields =
                    (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    public static OutputStream attemptToParseException(Exception exception){
        ByteArrayOutputStream streamTo = new ByteArrayOutputStream();
        attemptToParseException(exception, streamTo);
        return streamTo;
    }

    /**
     * Attempt to parse an exception to an {@link java.io.InputStream}. Does this by first looking for the method "getFaultInfo".
     * This is the standard method for the WS-N faults for retrieval of parseable information. If this is unsuccessful, or the method is not found,
     * the exception object itself is attempted parsed. If not found, or the method returned unparseable data, the method looks for
     * any method having a name containing the phrases fault or info in any order. If this yields no results or the method returned
     * data not parseable, all methods of the class are tried. If this yields no results, the same is tried for every field of
     * the class. If this yields no results, null is returned.
     * @param exception The exception to be parsed. This exception MUST be annotated with {@link javax.xml.ws.WebFault}.
     * @return An inputstream with the parsed data, or null if no data is found.
     */
    //TODO: Should we throw an IllegalArgumentException here, if we get an unparseable object?
    //TODO: Invoke proper method from an object factory
    public static void attemptToParseException(Exception exception, OutputStream streamTo){

        ObjectFactory soapObjectFactory = new ObjectFactory();

        WebFault webFaultAnnotation = (WebFault)findAnnotation(exception, WebFault.class);

        if(webFaultAnnotation == null){
            Log.e("Utilities.attemptToParseException", "The passed in exception[class = "+exception.getClass().getSimpleName()+"] does not carry ther WebFault annotation, please make sure all your parseable faults carries this annotation" +
                    "with specified name and namespace");
            throw new IllegalArgumentException("");
        }

        String faultName, namespaceName;
        if(webFaultAnnotation.targetNamespace().equals("")){
            Log.w("Utilities.attemptToParseException", "The passed in WebFault [class = "+exception.getClass().getSimpleName()+"] does not carry any namespace information, please consider setting the namespace() variable");
        }

        if(webFaultAnnotation.name().equals("")){
            Log.w("Utilities.attemptToParseException", "The passed in WebFault [class = "+exception.getClass().getSimpleName()+"] does not carry a name, please consider setting the name() variable.");
        }

        faultName = webFaultAnnotation.name();
        namespaceName = webFaultAnnotation.targetNamespace();

        /* Create fault-soap message */
        Envelope envelope = soapObjectFactory.createEnvelope();
        Body body = soapObjectFactory.createBody();
        Header header = soapObjectFactory.createHeader();
        Fault fault = soapObjectFactory.createFault();
        Detail detail = soapObjectFactory.createDetail();

        fault.setFaultcode(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"));
        fault.setFaultactor(exception.getMessage());
        fault.setDetail(detail);
        envelope.setBody(body);
        envelope.setHeader(header);

        JAXBElement toSend = soapObjectFactory.createEnvelope(envelope);

        Method method;
        Log.d("Utilities.attemptToParseException", "Got exception " + exception.getClass() + " to parse");

        try{
            detail.getAny().add(new JAXBElement(new QName(namespaceName, faultName), exception.getClass(), null, exception));

            fault.setFaultstring(exception.getMessage());

            body.getAny().add(new ObjectFactory().createFault(fault));
            XMLParser.writeObjectToStream(toSend, streamTo);
            return;
        /* We couldn't write it directly, lets try and get some information. Primarily by looking for a method named
        * getFaultInfo, then any other method named info, and then trying every other method */
        }catch(JAXBException e) {
            Log.d("Utilities.attemptToParseException", "Exception could not be parsed directly: " + e.getMessage());
        }

        /* Reset the detail and the body. And create a new stream */
        detail.getAny().clear();
        body.getAny().clear();

        /* This is default for all Oasis' exceptions */
        if(hasMethodWithName(exception.getClass(), "getFaultInfo")) {
            method = getMethodByName(exception.getClass(), "getFaultInfo");
            try {
                Object data = method.invoke(exception);
                detail.getAny().add(new JAXBElement(new QName(namespaceName, faultName), data.getClass(), null, data));
                fault.setFaultstring(exception.getMessage());
                body.getAny().add(new ObjectFactory().createFault(fault));
                XMLParser.writeObjectToStream(toSend, streamTo);
                return;
            } catch (Exception f) {
                f.printStackTrace();
                Log.d("Utilities.attemptToParseException", "getFaultInfo failed to parse: " + f.getMessage());
            }
        }

        detail.getAny().clear();
        body.getAny().clear();

        /* Tries for a method containing either fault or info in its name */
        if(hasMethodWithRegex(exception.getClass(), ".*((([Ff][Aa][Uu][Ll][Tt])|([Ii][Nn][Ff][Oo]))+).*")){
            method = getMethodByRegex(exception.getClass(), ".*((([Ff][Aa][Uu][Ll][Tt])|([Ii][Nn][Ff][Oo]))+).*");
            try {
                Object data = method.invoke(exception);
                detail.getAny().add(new JAXBElement(new QName(namespaceName, faultName), data.getClass(), null, data));
                fault.setFaultstring(exception.getMessage());
                body.getAny().add(new ObjectFactory().createFault(fault));
                XMLParser.writeObjectToStream(toSend, streamTo);
                return;
            }catch(Exception g){
                Log.d("Utilities.attemptToParseException", "Any fault/info function failed to prase: " + g.getMessage());
            }
        }

        detail.getAny().clear();
        body.getAny().clear();

        /* Try every method */
        for(Method method1 : exception.getClass().getMethods()) {
            try{
                Object data = method1.invoke(exception);
                detail.getAny().add(new JAXBElement(new QName(namespaceName, faultName), data.getClass(), null, data));
                fault.setFaultstring(exception.getMessage());
                body.getAny().add(new ObjectFactory().createFault(fault));
                XMLParser.writeObjectToStream(toSend, streamTo);
                return;
            }catch(Exception h){
                continue;
            }
        }

        detail.getAny().clear();
        body.getAny().clear();

        /* Try every field */
        for(Field field : getFieldsUpTo(exception.getClass(), null)){
            try {
                detail.getAny().add(new JAXBElement(new QName(namespaceName, faultName), field.getClass(), null, field));
                fault.setFaultstring(exception.getMessage());
                body.getAny().add(new ObjectFactory().createFault(fault));
                XMLParser.writeObjectToStream(toSend, streamTo);
                return;
            } catch (JAXBException e) {
                continue;
            }
        }
        throw new IllegalArgumentException("Object passed in is not parseable");
    }

    public static Object createSoapFault(String code) {
        ObjectFactory soapObjectFactory = new ObjectFactory();

        Envelope envelope = soapObjectFactory.createEnvelope();
        Body body = soapObjectFactory.createBody();
        Header header = soapObjectFactory.createHeader();
        Fault fault = soapObjectFactory.createFault();
        Detail detail = soapObjectFactory.createDetail();

        //TODO: Code can now be something non-standard, should we check for this?
        fault.setFaultcode(new QName("http://schemas.xmlsoap.org/soap/envelope/", code));
        fault.setDetail(detail);
        envelope.setBody(body);
        envelope.setHeader(header);

        JAXBElement toSend = soapObjectFactory.createEnvelope(envelope);
        return toSend;
    }
}
