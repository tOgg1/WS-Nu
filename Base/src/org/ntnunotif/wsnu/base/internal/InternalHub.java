package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Utilities;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;
import org.w3._2001._12.soap_envelope.Header;

import javax.rmi.CORBA.Util;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.ntnunotif.wsnu.base.internal.InternalMessage.*;

/**
 * The default Hub-implementation by WS-Nu. Implements the basic functionality of the hub-interface.
 * @author Tormod Haugland
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
        this._services = new ArrayList<WebServiceConnection>();
        this._server = ApplicationServer.getInstance();
        this._server.start(this);
    }

    public InternalHub(ApplicationServer server) throws Exception{
        this._services = new ArrayList<WebServiceConnection>();
        this._server = server;
        this._server.start(this);
    }

    /**
     * Stop the hub and its delegates.
     * @throws Exception
     */
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
    //TODO: Rethink the design of this method. Can everything be done sequentially? First STATUS_OK/FAILED handling and so on
    //TODO: Support multiple messages
    public InternalMessage acceptNetMessage(InputStream inputStream) {
        InternalMessage returnMessage = null;

        /* Decrypt message */
        InternalMessage parsedMessage;
        try {
            parsedMessage = XMLParser.parse(inputStream);
        } catch (JAXBException e) {
            //TODO: Move this handling to the parser?
            returnMessage = new InternalMessage(STATUS_FAULT_INTERNAL_ERROR
                    | STATUS_FAULT, null);
            e.printStackTrace();
            return returnMessage;
        }

        ArrayList<InternalMessage> outMessages = new ArrayList<>();

        try {
            System.out.println(parsedMessage.getMessage());
            Envelope env = (Envelope)((JAXBElement)parsedMessage.getMessage()).getValue();
            Header header = env.getHeader();
            Body body = env.getBody();
            List<Object> contents = body.getAny();

            for(Object content : contents){
                InternalMessage outMessage = new InternalMessage(parsedMessage.statusCode, content);
                outMessage.setNamespaceContext(parsedMessage.getNamespaceContext());
                outMessages.add(outMessage);
            }

        /* If this exception is thrown, the message received can not be soap */
        } catch (ClassCastException e) {
            return new InternalMessage(STATUS_FAULT_INVALID_PAYLOAD, null);
        }

        /* For all messages */
        for (InternalMessage outMessage : outMessages) {
            /* Try sending the message to everyone */
            for (WebServiceConnection service : _services) {

                /* Send the message forward */
                InternalMessage message = service.acceptMessage(outMessage);

                /* Incorrect destination */
                if ((message.statusCode & STATUS_INVALID_DESTINATION) > 0) {
                    continue;
                }

                if ((message.statusCode & STATUS_OK) > 0) {
                    if ((message.statusCode & STATUS_HAS_RETURNING_MESSAGE) > 0) {
                        /* This is easy, now we can convert it, and send it straight out*/
                        if ((message.statusCode & STATUS_RETURNING_MESSAGE_IS_OUTPUTSTREAM) > 0) {
                            try {
                                InputStream returningStream = Utilities.convertToInputStream((OutputStream) message.getMessage());
                                returnMessage = new InternalMessage(STATUS_OK
                                        | STATUS_HAS_RETURNING_MESSAGE
                                        | STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM, returningStream);
                                break;
                            } catch (ClassCastException e) {
                                System.err.println("Someone set the RETURNING_MESSAGE_IS_OUTPUTSTREAM flag when the message in the InternalMessage in fact was not");
                                e.printStackTrace();
                            }
                        /* Even better, the stream is already an inputstream */
                        } else if ((message.statusCode & STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM) > 0) {
                            try {
                                InputStream returningStream = (InputStream) message.getMessage();
                                returnMessage = new InternalMessage(STATUS_OK
                                        | STATUS_HAS_RETURNING_MESSAGE
                                        | STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM, returningStream);
                                break;
                            } catch (ClassCastException e) {
                                System.err.println("Someone set the RETURNING_MESSAGE_IS_INPUTSTREAM flag when the message in the InternalMessage in fact was not");
                                e.printStackTrace();
                            }
                        }

                        /* This is worse, now we have to find out what the payload is, and convert it to a stream*/
                        InputStream returningStream = Utilities.convertUnknownToInputStream(message.getMessage());

                        if (returningStream == null) {
                            System.err.println("Someone set the HAS_RETURNING_MESSAGE flag when there was no returning mesasge.");
                            returnMessage = new InternalMessage(STATUS_OK, null);
                            break;

                        } else {
                            returnMessage = new InternalMessage(STATUS_OK
                                    | STATUS_HAS_RETURNING_MESSAGE
                                    | STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM, returningStream);
                            break;
                        }
                    /* Everything is fine, and no message is to be returned */
                    } else {
                        returnMessage = new InternalMessage(STATUS_OK, null);
                    }
                } else if ((message.statusCode & STATUS_FAULT) > 0) {

                    /* There is not specified any specific fault, so we treat it as a generic fault */
                    if (message.statusCode == STATUS_FAULT) {
                        returnMessage = new InternalMessage(message.statusCode, null);
                        break;

                    } else if ((message.statusCode & STATUS_FAULT_INTERNAL_ERROR) > 0) {
                        returnMessage = new InternalMessage(message.statusCode, null);
                        break;

                    } else if ((message.statusCode & STATUS_FAULT_INVALID_PAYLOAD) > 0) {
                        returnMessage = new InternalMessage(message.statusCode, null);
                        break;

                    } else if ((message.statusCode & STATUS_FAULT_UNKNOWN_METHOD) > 0) {
                        returnMessage = new InternalMessage(message.statusCode, null);
                        break;
                    } else {
                        returnMessage = new InternalMessage(message.statusCode, null);
                        break;
                    }
                /* Something weird is going on, neither OK, INVALID_DESTINATION or FAULT is flagged*/
                } else {
                    returnMessage = new InternalMessage(STATUS_FAULT, null);
                    break;
                }
            }
            if (returnMessage == null) {
                returnMessage = new InternalMessage(STATUS_INVALID_DESTINATION, null);
            }
            return returnMessage;
        }
        if (returnMessage == null) {
            returnMessage = new InternalMessage(STATUS_INVALID_DESTINATION, null);
        }
        return returnMessage;
    }

    /**
     * Function to accept a message from a local service, and forward it out into the internet.
     * @param message The message to be sent out
     * @param endPoint The endpoint to send to
     */
    @Override
    public void acceptLocalMessage(InternalMessage message, String endPoint) {
        Object messageContent = message.getMessage();

        if((message.statusCode & STATUS_HAS_RETURNING_MESSAGE) > 0) {
            if ((message.statusCode & STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM) > 0) {
                InputStream messageAsStream = Utilities.convertUnknownToInputStream(messageContent);
            }
        }
    }

    /**
     * Extra method for adding several Web Services with an args parameter.
     * @param args
     */
    public void registerServices(WebServiceConnection... args){
        for(WebServiceConnection webServiceConnection : args){
            this.registerService(webServiceConnection);
        }
    }

    /**
     * Extra method for adding several Web Services with a collection
     * @param webServiceConnections
     */
    public void registerServices(Collection<WebServiceConnection> webServiceConnections){
        for(WebServiceConnection webServiceConnection : webServiceConnections){
            this.registerService(webServiceConnection);
        }
    }

    /**
     * Extra method for removing several Web Services with an args parameter.
     * @param args
     */
    public void removeServices(WebServiceConnection... args){
        for(WebServiceConnection webServiceConnection : args){
            this.removeService(webServiceConnection);
        }
    }

    /**
     * Extra method for removing several Web Services with a collection.
     * @param webServiceConnections
     */
    public void removeServices(Collection<WebServiceConnection> webServiceConnections){
        for(WebServiceConnection webServiceConnection : webServiceConnections){
            this.removeService(webServiceConnection);
        }
    }

    @Override
    public void registerService(WebServiceConnection webServiceConnection) {
        this._services.add(webServiceConnection);
    }

    @Override
    public void removeService(WebServiceConnection webServiceConnection) {
        this._services.remove(webServiceConnection);
    }

    @Override
    public boolean isServiceRegistered(WebServiceConnection webServiceConnection) {
        return this._services.contains(webServiceConnection);
    }
}
