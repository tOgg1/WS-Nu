package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
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

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * The default Hub-implementation by WS-Nu. Implements the basic functionality of the hub-interface.
 * This hub implements the acceptNetMessage by parsing every message to a soap-envelope object and sending it forward.
 * @author Tormod Haugland
 * Created by tormod on 3/3/14.
 */
public class ForwardingHub implements Hub {

    /**
     * List of internal web-service connections.
     */
    private ArrayList<ServiceConnection> _services;

    /**
     * Application-server object
     */
    private ApplicationServer _server;

    /**
     * Default constructor
     */
    public ForwardingHub() throws Exception {
        this._services = new ArrayList<ServiceConnection>();
        this._server = ApplicationServer.getInstance();
        this._server.start(this);
    }

    /**
     * Constructor with already existing server
     * @param server
     * @throws Exception
     */
    public ForwardingHub(ApplicationServer server) throws Exception{
        this._services = new ArrayList<ServiceConnection>();
        this._server = server;
        this._server.start(this);
    }

    /**
     * Takes an InternalMessage with a wrapped soap.envelope of some form, unpacks it, and sends it forward in the system.
     * @param internalMessage
     * @return
     */
    @Override
    //TODO: Rethink the design of this method. Can everything be done sequentially? First STATUS_OK/FAILED handling and so on
    //TODO: Support multiple messages
    public InternalMessage acceptNetMessage(InternalMessage internalMessage) {
        InternalMessage returnMessage;
        RequestInformation requestInformation = internalMessage.getRequestInformation();

        /* We dont have any content, but perhaps a request? */
        if((internalMessage.statusCode & InternalMessage.STATUS_HAS_MESSAGE) == 0){
            for (ServiceConnection service : _services) {
                return service.acceptRequest(internalMessage);
            }
            return new InternalMessage(STATUS_FAULT | STATUS_INVALID_DESTINATION, null);
        }else{

            InputStream stream = (InputStream)internalMessage.getMessage();

        /* Decrypt message */
            InternalMessage parsedMessage;
            try {
                parsedMessage = XMLParser.parse(stream);
                requestInformation.setNamespaceContext(parsedMessage.getRequestInformation().getNamespaceContext());
            } catch (JAXBException e) {
                returnMessage = new InternalMessage(STATUS_FAULT_INTERNAL_ERROR | STATUS_FAULT, null);
                e.printStackTrace();
                return returnMessage;
            }

            Envelope envelope;
            try {
                envelope = (Envelope)((JAXBElement)parsedMessage.getMessage()).getValue();

            /* If this exception is thrown, the message received can not be soap */
            } catch (ClassCastException e) {
                return new InternalMessage(STATUS_FAULT_INVALID_PAYLOAD, null);
            }

            if(envelope == null){
                return new InternalMessage(STATUS_FAULT, null);
            }

            Log.d("Hub", "Attempting to send message");
        /* Try sending the message to everyone */
            for (ServiceConnection service : _services) {

                Log.d("Hub", "Forwarding message....");
            /* Send the message forward */
                InternalMessage outMessage = new InternalMessage(STATUS_OK|STATUS_ENDPOINTREF_IS_SET, envelope);
                outMessage.setRequestInformation(requestInformation);
                InternalMessage message = service.acceptMessage(outMessage);

            /* Incorrect destination */
                if ((message.statusCode & STATUS_INVALID_DESTINATION) > 0) {
                    continue;
                }

                if ((message.statusCode & STATUS_OK) > 0) {
                    if ((message.statusCode & STATUS_HAS_MESSAGE) > 0) {
                    /* This is easy, now we can convert it, and send it straight out*/
                        if ((message.statusCode & STATUS_MESSAGE_IS_OUTPUTSTREAM) > 0) {
                            try {
                                InputStream returningStream = Utilities.convertToInputStream((OutputStream) message.getMessage());
                                return new InternalMessage(STATUS_OK
                                        | STATUS_HAS_MESSAGE
                                        | STATUS_MESSAGE_IS_INPUTSTREAM, returningStream);
                            } catch (ClassCastException e) {
                                Log.e("Hub", "Someone set the RETURNING_MESSAGE_IS_OUTPUTSTREAM flag when the message in the InternalMessage in fact was not");
                                e.printStackTrace();
                            }
                        /* Even better, the stream is already an inputstream */
                        } else if ((message.statusCode & STATUS_MESSAGE_IS_INPUTSTREAM) > 0) {
                            try {
                                InputStream returningStream = (InputStream) message.getMessage();
                                return new InternalMessage(STATUS_OK
                                        | STATUS_HAS_MESSAGE
                                        | STATUS_MESSAGE_IS_INPUTSTREAM, returningStream);
                            } catch (ClassCastException e) {
                                Log.e("Hub", "Someone set the RETURNING_MESSAGE_IS_INPUTSTREAM flag when the message in the InternalMessage in fact was not");
                                e.printStackTrace();
                            }
                        }

                        /* This is worse, now we have to find out what the payload is, and convert it to a stream*/
                        InputStream returningStream = Utilities.convertUnknownToInputStream(message.getMessage());

                        if (returningStream == null) {
                            Log.e("Hub", "Someone set the HAS_RETURNING_MESSAGE flag when there was no returning mesasge.");
                            return new InternalMessage(STATUS_OK, null);


                        } else {
                            return new InternalMessage(STATUS_OK
                                    | STATUS_HAS_MESSAGE
                                    | STATUS_MESSAGE_IS_INPUTSTREAM, returningStream);

                        }
                /* Everything is fine, and no message is to be returned */
                    } else {
                        return new InternalMessage(STATUS_OK, null);
                    }
                } else if ((message.statusCode & STATUS_FAULT) > 0) {

                /* There is not specified any specific fault, so we treat it as a generic fault */
                    if (message.statusCode == STATUS_FAULT) {
                        return new InternalMessage(message.statusCode, null);


                    } else if ((message.statusCode & STATUS_FAULT_INTERNAL_ERROR) > 0) {
                        return new InternalMessage(message.statusCode, null);


                    } else if ((message.statusCode & STATUS_FAULT_INVALID_PAYLOAD) > 0) {
                        return new InternalMessage(message.statusCode, null);

                    } else if ((message.statusCode & STATUS_FAULT_UNKNOWN_METHOD) > 0) {
                        return new InternalMessage(message.statusCode, null);

                    } else {
                        return new InternalMessage(message.statusCode, null);

                    }
            /* Something weird is going on, neither OK, INVALID_DESTINATION or FAULT is flagged*/
                } else {
                    return new InternalMessage(STATUS_FAULT, null);

                }
            }
            return new InternalMessage(STATUS_FAULT_INTERNAL_ERROR, null);
        }
    }

    /**
     * Stop the hub and its delegates.
     * @throws Exception
     */
    public void stop(){

        /* Enforce garbage collection */
        _server.stop();
        _server = null;

        _services.clear();
        _services = null;
    }

    /**
     * Function to accept a message from a local service, and forward it out into the internet.
     * @param message The message to be sent out
     */
    //TODO: Generate meaningful soap headers
    @Override
    public InternalMessage acceptLocalMessage(InternalMessage message) {
        Object messageContent = message.getMessage();

        if((message.statusCode & STATUS_HAS_MESSAGE) > 0) {
            /* Easy if it already is an inputstream */
            if((message.statusCode & STATUS_MESSAGE_IS_INPUTSTREAM) > 0) {
                try{
                    InputStream messageAsStream = (InputStream)messageContent;
                    message.setMessage(messageAsStream);
                    return _server.sendMessage(message);
                }catch(ClassCastException e){
                    e.printStackTrace();
                    Log.e("Hub", "Someone set the RETURNING_MESSAGE_IS_INPUTSTREAM when in fact it wasn't.");
                    return new InternalMessage(STATUS_FAULT_INVALID_PAYLOAD|STATUS_FAULT, null);
                }
            } else if((message.statusCode & STATUS_MESSAGE_IS_OUTPUTSTREAM) > 0) {
                    InputStream messageAsStream = Utilities.convertToInputStream((OutputStream) messageContent);
                    message.setMessage(messageAsStream);
                return _server.sendMessage(message);
            /* This is worse */
            }else{
                Envelope envelope = new Envelope();
                Header header = new Header();
                Body body = new Body();
                body.getAny().add(messageContent);
                envelope.setBody(body);
                envelope.setHeader(header);

                InputStream messageAsStream = Utilities.convertUnknownToInputStream(envelope);
                message.setMessage(messageAsStream);
                return _server.sendMessage(message);
            }
        }else{
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
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
    public void registerService(ServiceConnection webServiceConnector) {
        this._services.add(webServiceConnector);
    }

    @Override
    public void removeService(ServiceConnection webServiceConnector) {
        this._services.remove(webServiceConnector);
    }

    @Override
    public boolean isServiceRegistered(ServiceConnection webServiceConnector) {
        return this._services.contains(webServiceConnector);
    }

    @Override
    public Collection<ServiceConnection> getServices() {
        return _services;
    }

    @Override
    public String getInetAdress() {
        return ApplicationServer.getURI();
    }
}
