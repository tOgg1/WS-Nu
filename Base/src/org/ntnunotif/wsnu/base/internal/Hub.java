package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.InternalMessage;

import java.io.InputStream;
import java.util.Collection;

/**
 * Interface for a hub. Implementations of this interface should be able to receive net-messages, and local messages (from connected web services)
 * @author Tormod Haugland
 * Created by tormod on 3/3/14.
 */
public interface Hub {

    /**
     * Function to accept a message from the net.
     * @return Returns the message(s) that is going back
     */
    public InternalMessage acceptNetMessage(InternalMessage message);

    /**
     * Function to accept a message from a local service, and forward it out into the internet.
     */
    public void acceptLocalMessage(InternalMessage message);

    /**
     * Get the address this server is currently running on.
     * @return
     */
    public String getInetAdress();

    /**
     * Register's a service for usage with this hub object.
     * @param webServiceConnector
     */
    public void registerService(ServiceConnection webServiceConnector);

    /**
     * Removes a registered service for usage with this hub object.
     * @param webServiceConnector
     */
    public void removeService(ServiceConnection webServiceConnector);

    /**
     * Checks if the webServiceConnector is registered with the hub
     * @param webServiceConnector
     * @return
     */
    public boolean isServiceRegistered(ServiceConnection webServiceConnector);

    public Collection<ServiceConnection> getServices();
}
