package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by tormod on 3/3/14.
 */
public class InternalHub implements Hub {

    /**
     * List of internal web-service connections.
     */
    private ArrayList<WebServiceConnection> _services;

    /**
     * Application-server object
     */
    private ApplicationServer _server;


    /**
     * Default constructor
     */
    public InternalHub() throws Exception {
        _server = ApplicationServer.getInstance();
        _services = new ArrayList<WebServiceConnection>();
        _server.start(this);
    }

    public void stop() throws Exception {

        // Enforce garbage collection
        _server.stop();
        _server = null;

        _services.clear();
        _services = null;
    }

    /**
     * Takes a net-message, depacks it (parses it), and sends it forward in the system
     */
    @Override
    public void acceptNetMessage(InputStream inputStream){
        /* Decrypt message */
        Object parsedObject = null;
        try {
            parsedObject = XMLParser.parse(inputStream);
        } catch (JAXBException e) {
            //TODO: Send some fault from the specification that I cant remember right now
            e.printStackTrace();
        }

        /* Find out where to send the message */
        

        /* Send the message forward */
        for(WebServiceConnection service : _services){
            service.acceptMessage(parsedObject);
        }
    }

    @Override
    public void acceptLocalMessage(Object object, String endPoint) {
        ByteArrayOutputStream generatedMessage = new ByteArrayOutputStream();
        try {
            XMLParser.writeObjectToStream(object, generatedMessage);
        } catch (JAXBException e) {
            System.err.println("Error parsing object from web service, is the service sending a valid WSN-object for parsing?");
            e.printStackTrace();
        }


    }
}