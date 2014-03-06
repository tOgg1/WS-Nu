package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.XMLParser;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by tormod on 3/3/14.
 */
public interface Connector {

    /**
     * Function to accept a message from the net
     */
    public void acceptNetMessage(Object object);

    /**
     * Function to accept a message from a local service
     */
    public void acceptLocalMessage(Object object);
}
