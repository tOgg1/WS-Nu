package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.topics.TopicValidator;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by tormod on 3/3/14.
 */
public class Bus implements Connector{

    /**
     * List of internal web-service connections.
     */
    private ArrayList<WebServiceConnection> _services;

    /**
     * Application-server object
     */
    private ApplicationServer _server;

    /**
     * Topic-validator object
     */
    private TopicValidator _topicValidator;


    /**
     * Default constructor
     */
    public Bus(){
        _server = new ApplicationServer();
        _services = new ArrayList<WebServiceConnection>();
        _topicValidator = new TopicValidator();
    }

    /**
     * Takes a net-message, depacks it (parses it), and sends it forward in the system
     */
    @Override
    public void acceptNetMessage(Object object){
        // Decrypt message
        try {
            Object parsedObject = XMLParser.parse((InputStream)object);
        } catch (JAXBException e) {
            //TODO: Send some fault from the specification that I cant remember right now
            e.printStackTrace();
        }

        // Send the message forward
        for(WebServiceConnection connection : _services){

        }
    }

    @Override
    public void acceptLocalMessage(Object object) {

    }
}
