package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Utilities;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;
import org.w3._2001._12.soap_envelope.Header;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.ntnunotif.wsnu.base.internal.InternalMessage.*;

/**
 * The default Hub-implementation by WS-Nu. Implements the basic functionality of the hub-interface.
 * This hub implements the acceptNetMessage by parsing every message to a soap-envelope object and sending it forward.
 * @author Tormod Haugland
 * Created by tormod on 3/3/14.
 */
public class DefaultHub implements Hub {

    /**
     * List of internal web-service connections.
     */
    private ArrayList<WebServiceConnector> _services;

    /**
     * Application-server object
     */
    private ApplicationServer _server;

    /**
     * Default constructor
     */
    public DefaultHub() throws Exception {
        this._services = new ArrayList<WebServiceConnector>();
        this._server = ApplicationServer.getInstance();
        this._server.start(this);
    }

    /**
     * Constructor with already existing server
     * @param server
     * @throws Exception
     */
    public DefaultHub(ApplicationServer server) throws Exception{
        this._services = new ArrayList<WebServiceConnector>();
        this._server = server;
        this._server.start(this);
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
            returnMessage = new InternalMessage(STATUS_FAULT_INTERNAL_ERROR | STATUS_FAULT, null);
            e.printStackTrace();
            return returnMessage;
        }

        ArrayList<InternalMessage> outMessages = new ArrayList<>();
        Envelope envelope;
        try {
            System.out.println(parsedMessage.getMessage());
            envelope = (Envelope)((JAXBElement)parsedMessage.getMessage()).getValue();

        /* If this exception is thrown, the message received can not be soap */
        } catch (ClassCastException e) {
            return new InternalMessage(STATUS_FAULT_INVALID_PAYLOAD, null);
        }

        if(envelope == null){
            return new InternalMessage(STATUS_FAULT, null);
        }

        /* Try sending the message to everyone */
        for (WebServiceConnector service : _services) {

            /* Send the message forward */
            InternalMessage message = service.acceptMessage(new InternalMessage(STATUS_OK, envelope));

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
                            return new InternalMessage(STATUS_OK
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
                            return new InternalMessage(STATUS_OK
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
                        return new InternalMessage(STATUS_OK, null);
                        break;

                    } else {
                        return new InternalMessage(STATUS_OK
                                | STATUS_HAS_RETURNING_MESSAGE
                                | STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM, returningStream);
                        break;
                    }
                /* Everything is fine, and no message is to be returned */
                } else {
                    return new InternalMessage(STATUS_OK, null);
                }
            } else if ((message.statusCode & STATUS_FAULT) > 0) {

                /* There is not specified any specific fault, so we treat it as a generic fault */
                if (message.statusCode == STATUS_FAULT) {
                    return new InternalMessage(message.statusCode, null);
                    break;

                } else if ((message.statusCode & STATUS_FAULT_INTERNAL_ERROR) > 0) {
                    return new InternalMessage(message.statusCode, null);
                    break;

                } else if ((message.statusCode & STATUS_FAULT_INVALID_PAYLOAD) > 0) {
                    return new InternalMessage(message.statusCode, null);
                    break;

                } else if ((message.statusCode & STATUS_FAULT_UNKNOWN_METHOD) > 0) {
                    return new InternalMessage(message.statusCode, null);
                    break;
                } else {
                    return new InternalMessage(message.statusCode, null);
                    break;
                }
            /* Something weird is going on, neither OK, INVALID_DESTINATION or FAULT is flagged*/
            } else {
                return new InternalMessage(STATUS_FAULT, null);
                break;
            }
        }
        return new InternalMessage(STATUS_FAULT_INTERNAL_ERROR, null);
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
     * Function to accept a message from a local service, and forward it out into the internet.
     * @param message The message to be sent out
     * @param endPoint The endpoint to send to
     */
    @Override
    public void acceptLocalMessage(InternalMessage message, String endPoint) {
        Object messageContent = message.getMessage();

        if((message.statusCode & STATUS_HAS_RETURNING_MESSAGE) > 0) {
            if((message.statusCode & STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM) > 0) {
                try{
                    InputStream messageAsStream = (InputStream)messageContent;
                    _server.sendMessage(new InternalMessage(STATUS_OK|STATUS_HAS_RETURNING_MESSAGE, message), endPoint);
                }catch(ClassCastException e){
                    e.printStackTrace();
                    System.err.println("Someone set the RETURNING_MESSAGE_IS_INPUTSTREAM when in fact it wasn't.");
                }
            } else if((message.statusCode & STATUS_RETURNING_MESSAGE_IS_OUTPUTSTREAM) > 0) {
                    InputStream messageAsStream = Utilities.convertToInputStream((OutputStream) messageContent);
                    _server.sendMessage(new InternalMessage(STATUS_OK|STATUS_HAS_RETURNING_MESSAGE, message), endPoint);
            }


        }
    }

    /**
     * Extra method for adding several Web Services with an args parameter.
     * @param args
     */
    public void registerServices(WebServiceConnector... args){
        for(WebServiceConnector webServiceConnector : args){
            this.registerService(webServiceConnector);
        }
    }

    /**
     * Extra method for adding several Web Services with a collection
     * @param webServiceConnectors
     */
    public void registerServices(Collection<WebServiceConnector> webServiceConnectors){
        for(WebServiceConnector webServiceConnector : webServiceConnectors){
            this.registerService(webServiceConnector);
        }
    }

    /**
     * Extra method for removing several Web Services with an args parameter.
     * @param args
     */
    public void removeServices(WebServiceConnector... args){
        for(WebServiceConnector webServiceConnector : args){
            this.removeService(webServiceConnector);
        }
    }

    /**
     * Extra method for removing several Web Services with a collection.
     * @param webServiceConnectors
     */
    public void removeServices(Collection<WebServiceConnector> webServiceConnectors){
        for(WebServiceConnector webServiceConnector : webServiceConnectors){
            this.removeService(webServiceConnector);
        }
    }

    @Override
    public void registerService(WebServiceConnector webServiceConnector) {
        this._services.add(webServiceConnector);
    }

    @Override
    public void removeService(WebServiceConnector webServiceConnector) {
        this._services.remove(webServiceConnector);
    }

    @Override
    public boolean isServiceRegistered(WebServiceConnector webServiceConnector) {
        return this._services.contains(webServiceConnector);
    }
}
