package org.ntnunotif.wsnu.base.util;

import com.google.common.collect.Lists;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.w3._2001._12.soap_envelope.*;

import javax.xml.bind.JAXBException;
import java.io.*;
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
     * Function to get ALL fiels up the class hierarcy.
     * Credit: John B @ stackoverflow: http://stackoverflow.com/questions/16966629/what-is-the-difference-between-getfields-getdeclaredfields-in-java-reflection
     * @param startClass Class to get all methods for
     * @param exclusiveParent Any parentclass that is to be excluded from the retrieval.
     * @return
     */
    public static Iterable<Field> getFieldsUpTo(@NotNull Class<?> startClass,
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

    /**
     * Attempt to parse an exception to an {@link java.io.InputStream}. Does this by first looking for the method "getFaultInfo".
     * This is the standard method for the WS-N faults for retrieval of parseable information. If this is unsuccessful, or the method is not found,
     * the exception object itself is attempted parsed. If not found, or the method returned unparseable data, the method looks for
     * any method having a name containing the phrases fault or info in any order. If this yields no results or the method returned
     * data not parseable, all methods of the class are tried. If this yields no results, the same is tried for every field of
     * the class. If this yields no results, null is returned.
     * @param exception The exception to be parsed
     * @return An inputstream with the parsed data, or null if no data is found.
     */
    //TODO: Should we throw an IllegalArgumentException here, if we get an unparseable object?
    //TODO: Make the parser parse the faults...
    public static InputStream attemptToParseException(Exception exception){

        ObjectFactory soapObjectFactory = new ObjectFactory();

        /* Create fault-soap message */
        Envelope envelope = new Envelope();
        Body body = new Body();
        Header header = new Header();
        Fault fault = new Fault();
        Detail detail = new Detail();

        fault.setFaultactor(exception.getMessage());
        fault.setDetail(detail);
        body.getAny().add(fault);
        envelope.setBody(body);
        envelope.setHeader(header);

        Method method;
        ByteArrayOutputStream stream = null;
        stream = new ByteArrayOutputStream();
        Log.d("Utilities.attemptToParseException", "Got exception " + exception.getClass() + " to parse");

        try{
            detail.getAny().add(exception);

            stream = new ByteArrayOutputStream();
            XMLParser.writeObjectToStream(envelope, stream);
            return convertToByteArrayInputStream(stream);
        /* We couldn't write it directly, lets try and get some information. Primarily by looking for a method named
        * getFaultInfo, then any other method named info, and then trying every other method */
        }catch(JAXBException e) {
            Log.d("Utilities.attemptToParseException", "Exception could not be parsed directly: " + e.getMessage());
        }

        detail.getAny().clear();

        /* This is default for all Oasis' exceptions */
        if(hasMethodWithName(exception.getClass(), "getFaultInfo")) {
            method = getMethodByName(exception.getClass(), "getFaultInfo");
            try {
                detail.getAny().add(method.invoke(exception));
                XMLParser.writeObjectToStream(envelope, stream);
                return convertToByteArrayInputStream(stream);
            } catch (Exception f) {
                f.printStackTrace();
                Log.d("Utilities.attemptToParseException", "getFaultInfo failed to parse: " + f.getMessage());
            }
        }

        detail.getAny().clear();

        /* Tries for a method containing either fault or info in its name */
        if(hasMethodWithRegex(exception.getClass(), ".*((([Ff][Aa][Uu][Ll][Tt])|([Ii][Nn][Ff][Oo]))+).*")){
            method = getMethodByRegex(exception.getClass(), ".*((([Ff][Aa][Uu][Ll][Tt])|([Ii][Nn][Ff][Oo]))+).*");
            try {
                detail.getAny().add(method.invoke(exception));
                XMLParser.writeObjectToStream(envelope, stream);
                return convertToByteArrayInputStream(stream);
            }catch(Exception g){
                Log.d("Utilities.attemptToParseException", "Any fault/info function failed to prase: " + g.getMessage());
            }
        }

        detail.getAny().clear();

        /* Try every method */
        for(Method method1 : exception.getClass().getMethods()) {
            try{
                detail.getAny().add(method1.invoke(exception));
                XMLParser.writeObjectToStream(envelope, stream);
                return convertToByteArrayInputStream(stream);
            }catch(Exception h){
                continue;
            }
        }

        detail.getAny().clear();

        /* Try every field */
        for(Field field : getFieldsUpTo(exception.getClass(), null)){
            try {
                detail.getAny().add(field);
                XMLParser.writeObjectToStream(envelope, stream);
                return convertToByteArrayInputStream(stream);
            } catch (JAXBException e) {
                continue;
            }
        }

    return null;
    }
}
