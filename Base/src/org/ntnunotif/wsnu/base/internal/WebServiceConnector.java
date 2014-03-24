package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.InternalMessage;

/**
 * @author Tormod Haugland
 * Created by tormod on 3/3/14.
 */
public interface WebServiceConnector {

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
