package org.ntnunotif.wsnu.base.util;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.sun.istack.internal.Nullable;
import org.ntnunotif.wsnu.base.net.XMLParser;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
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
        byte[] bytes = stream.toByteArray();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Function to get ALL fiels up the class hierarcy.
     * Credit: John B @ stackoverflow: http://stackoverflow.com/questions/16966629/what-is-the-difference-between-getfields-getdeclaredfields-in-java-reflection
     * @param startClass
     * @param exclusiveParent
     * @return
     */
    public static Iterable<Field> getFieldsUpTo(Class<?> startClass,
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
}
