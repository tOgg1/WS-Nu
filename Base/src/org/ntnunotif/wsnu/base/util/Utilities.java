package org.ntnunotif.wsnu.base.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility function for the Base module.
 * @author Tormod Haugland
 * Created by tormod on 3/3/14.
 */
public class Utilities {

    /**
     * Takes an outputstream with data. Then prepares an inputstream with this data and
     * @param stream
     * @return
     */
    public static InputStream convertToInputStream(OutputStream stream){
        //TODO:
        return null;
    }


    /**
     * Takes any object, and tries to convert its data to an inputstream.
     * @param message
     * @return
     */
    public static InputStream convertUnknownToInputStream(Object message) {
        //TODO:
        return null;
    }
}
