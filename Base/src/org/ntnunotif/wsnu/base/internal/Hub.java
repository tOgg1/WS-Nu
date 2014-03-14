package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.XMLParser;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.ArrayList;

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
    public ArrayList<InternalMessage> acceptNetMessage(InputStream inputStream);

    /**
     * Function to accept a message from a local service, and forward it out into the internet.
     */
    public void acceptLocalMessage(Object object, String endPoint);
}
