package org.ntnunotif.wsnu.base.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by tormod on 3/3/14.
 */
public class Utilities {

    public static final int TYPE_CONSUMER = 0x01;
    public static final int TYPE_PRODUCER = 0x02;
    public static final int TYPE_BROKER = 0x03;
    public static final int TYPE_SUBSCRIPTIONMANAGER = 0x04;

    public static final int FUNCTIONALITY_CONSUMER_SOMEFUNC1 = 0x01;
    public static final int FUNCTIONALITY_CONSUMER_SOMEFUNC2 = 0x02;
    public static final int FUNCTIONALITY_CONSUMER_SOMEFUNC3 = 0x04;
    public static final int FUNCTIONALITY_CONSUMER_SOMEFUNC4 = 0x08;
    public static final int FUNCTIONALITY_CONSUMER_SOMEFUNC5 = 0x10;
    public static final int FUNCTIONALITY_CONSUMER_SOMEFUNC6 = 0x20;
    public static final int FUNCTIONALITY_CONSUMER_SOMEFUNC7 = 0x40;
    public static final int FUNCTIONALITY_CONSUMER_SOMEFUNC8 = 0x80;

    //TODO: Add functionality flags for producer and brokers

    /**
     * Takes an outputstream with data. Then prepares an inputstream with this data and
     * @param stream
     * @return
     */
    public static InputStream convertToInputStream(OutputStream stream){
        //TODO:
        return null;
    }

}
