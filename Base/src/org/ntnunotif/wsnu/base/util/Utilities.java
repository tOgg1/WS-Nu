package org.ntnunotif.wsnu.base.util;

import com.google.common.io.ByteStreams;
import org.ntnunotif.wsnu.base.net.XMLParser;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

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
}
