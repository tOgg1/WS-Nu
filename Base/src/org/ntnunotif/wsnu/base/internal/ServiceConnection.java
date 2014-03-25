package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.InternalMessage;

/**
 * Created by tormod on 25.03.14.
 */
public interface ServiceConnection {

    /**
     * Accept a message from a hub. This function forwards the message to its connected Web Service.
     * @param message
     * @return Returns the appropriate response, null if no response is expected, or the message could not be processed
     */
    public InternalMessage acceptMessage(InternalMessage message);

    /**
     * Return the type of the Web Service connected to this connection.
     * @return The type as a class
     */
    public Class getServiceType();

    /**
     * Return the functionality this Web Service offers.
     * @return Return the allowed functionality
     */
    public Object getServiceFunctionality();
}
