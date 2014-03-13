package org.ntnunotif.wsnu.base.internal;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by tormod on 3/3/14.
 */
public interface WebServiceConnection {

    /**
     * Accept a message from a hub. This function forwards the message to its connected Web Service.
     * @param message
     * @return Returns the appropriate response, null if no response is expected
     */
    public Object acceptMessage(Object message);

    /**
     * Return the type of the Web Service connected to this connection.
     * @return The type as a flag
     */
    public Class getServiceType();

    /**
     * Return the functionality this Web Service offers.
     * @return The methods allowed by the Web Service
     */
    public HashMap<String, Method> getServiceFunctionality();
}
