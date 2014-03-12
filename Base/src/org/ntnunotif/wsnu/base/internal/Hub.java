package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.XMLParser;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by tormod on 3/3/14.
 */
public interface Hub {

    /**
     * Function to accept a message from the net
     */
    public void acceptNetMessage(InputStream inputStream);

    /**
     * Function to accept a message from a local service
     */
    public void acceptLocalMessage(InputStream inputStream);
}
