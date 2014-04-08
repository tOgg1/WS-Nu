package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.Utilities;
import org.xmlsoap.schemas.soap.envelope.Body;
import org.xmlsoap.schemas.soap.envelope.Envelope;
import org.xmlsoap.schemas.soap.envelope.Header;

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
public class SoapForwardingHub implements Hub {

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
    public SoapForwardingHub() throws Exception {
        this._services = new ArrayList<ServiceConnection>();
        this._server = ApplicationServer.getInstance();
        this._server.start(this);
    }

    /**
     * Constructor with already existing server
     * @param server
     * @throws Exception
     */
    public SoapForwardingHub(ApplicationServer server) throws Exception{
        this._services = new ArrayList<ServiceConnection>();
        this._server = server;
        this._server.start(this);
    }

    /**
     * Takes an InternalMessage with a wrapped soap.envelope of some form, unpacks it, and sends it forward in the system.
     * If the requested webservice (per uri) is not found, the hub will try sending the message until someone accepts it.
     * @param internalMessage
     * @return
     */
    @Override
    public InternalMessage acceptNetMessage(InternalMessage internalMessage, OutputStream streamToRequestor) {
        ServiceConnection connection = findRecipient(internalMessage.getRequestInformation().getRequestURL());

        boolean foundConnection = connection != null;
        /* Initialize the value with invalid destination, in case no one accepts it*/
        InternalMessage returnMessage = new InternalMessage(STATUS_FAULT| STATUS_FAULT_INVALID_DESTINATION, null);

        /* If this is just a request-message and has no content */
        if((internalMessage.statusCode & STATUS_HAS_MESSAGE) == 0){
            Log.d("SoapForwardingHub", "Forwarding request");
            if(foundConnection){
                returnMessage = connection.acceptRequest(internalMessage);
            }else{
                for(ServiceConnection service : _services){
                    returnMessage = service.acceptRequest(internalMessage);
                    if((returnMessage.statusCode & STATUS_FAULT_INVALID_DESTINATION) > 0){
                        continue;
                    }else if((returnMessage.statusCode & STATUS_OK) > 0){
                        break;
                    }else if((returnMessage.statusCode & STATUS_FAULT_INTERNAL_ERROR) > 0){
                        break;
                    }
                    break;
                }
            }
        /* We have content and should deal with it */
        }else{
            Log.d("SoapForwardingHub", "Forwarding message");
            InternalMessage parsedMessage;
            //Envelope envelope;
            /* Try to parse and cast the message to a soap-envelope */
            try {
                parsedMessage = XMLParser.parse((InputStream) internalMessage.getMessage());
                try {
                    // Check if message is any of the supported SOAP envelopes
                    JAXBElement message = (JAXBElement)parsedMessage.getMessage();
                    Class messageClass = message.getDeclaredType();
                    if (messageClass.equals(org.w3._2001._12.soap_envelope.Envelope.class)) {

                        /* Re-use internalMessage object for optimization */
                        internalMessage.setMessage(message.getValue());
                    } else if (messageClass.equals(org.xmlsoap.schemas.soap.envelope.Envelope.class)) {

                        /* Re-use internalMessage object for optimization */
                        internalMessage.setMessage(message.getValue());
                    }
                }catch(ClassCastException e){
                    Log.e("SoapForwardingHub","Failed to cast message to a SOAP envelope");
                    return new InternalMessage(STATUS_FAULT | STATUS_FAULT_INVALID_PAYLOAD, null);
                }
            } catch (JAXBException e) {
                Log.e("SoapForwardingHub", "Parse error: " + e.getMessage());
                return new InternalMessage(STATUS_FAULT_INTERNAL_ERROR | STATUS_FAULT, null);
            }

            /* Re-use internalMessage object for optimization */
            internalMessage.statusCode = STATUS_OK | STATUS_HAS_MESSAGE | STATUS_ENDPOINTREF_IS_SET;

            if(foundConnection){
                returnMessage = connection.acceptMessage(internalMessage);
            }else{
                for(ServiceConnection service : _services){
                    returnMessage = service.acceptMessage(internalMessage);
                    if((returnMessage.statusCode & STATUS_FAULT_INVALID_DESTINATION) > 0){
                        continue;
                    }else if((returnMessage.statusCode & STATUS_OK) > 0){
                        break;
                    }else if((returnMessage.statusCode & STATUS_FAULT_INTERNAL_ERROR) > 0){
                        break;
                    }
                }
            }
        }

        /* Everything is processed properly, and we can figure out what to return */
        if((returnMessage.statusCode & STATUS_OK) > 0){
            /* If we have a message we should try and convert it to an inputstream before returning
            * Notably the ApplicationServer does accept other form of messages, but it is more logical to conert
            * it at this point */
            if((returnMessage.statusCode & STATUS_HAS_MESSAGE) > 0){
                Log.d("SoapForwardingHub", "Returning message");
                if((returnMessage.statusCode & STATUS_MESSAGE_IS_INPUTSTREAM) > 0){
                    try{
                        InputStream stream = (InputStream)returnMessage.getMessage();
                        returnMessage.setMessage(stream);
                    }catch(ClassCastException e){
                        Log.e("SoapForwardingHub", "Casting the returnMessage to InputStream failed, even though someone set the MESSAGE_IS_INPUTSTREAM flag");
                        return new InternalMessage(STATUS_FAULT | STATUS_FAULT_INTERNAL_ERROR, null);
                    }
                }else if((returnMessage.statusCode & STATUS_MESSAGE_IS_OUTPUTSTREAM) > 0){
                    try{
                        InputStream stream = Utilities.convertToInputStream((OutputStream) returnMessage.getMessage());
                        returnMessage.setMessage(stream);
                    }catch(ClassCastException e){
                        Log.e("SoapForwardingHub", "Casting the returnMessage to OutputStream failed, even though someone set the MESSAGE_IS_OUTPUSTREAM flag");
                        return new InternalMessage(STATUS_FAULT | STATUS_FAULT_INTERNAL_ERROR, null);
                    }
                }else{
                    Object messageToParse;
                    if(!((returnMessage.getMessage() instanceof org.w3._2001._12.soap_envelope.Envelope) ||
                            returnMessage.getMessage() instanceof Envelope)){
                        org.w3._2001._12.soap_envelope.Envelope env = new org.w3._2001._12.soap_envelope.Envelope();
                        org.w3._2001._12.soap_envelope.Body body = new org.w3._2001._12.soap_envelope.Body();
                        body.getAny().add(returnMessage.getMessage());
                        env.setBody(body);
                        messageToParse = env;
                    }else{
                        messageToParse = returnMessage.getMessage();
                    }

                    /* Try to parse the object directly into the OutputStream passed in*/
                    try{
                        XMLParser.writeObjectToStream(messageToParse, streamToRequestor);
                        returnMessage.statusCode = STATUS_OK;
                    /* This was not do-able*/
                    }catch(JAXBException e){
                        Log.e("SoapForwardingHub", "Unable to marshal returnMessage. Consider converting the message-paylod at an earlier point.");
                        return new InternalMessage(STATUS_FAULT | STATUS_FAULT_INTERNAL_ERROR, null);
                    }
                }
                return returnMessage;
            /* We have no message and can just return */
            }else{
                Log.d("SoapForwardingHub", "Returning nothing");
                return returnMessage;
            }
        /* Something went wrong up the stack, but we're not gonna meddle with it here, return the message back to the applicationserver
         * and let it figure out what error message to send back */
        }else{
            if((returnMessage.statusCode & STATUS_EXCEPTION_SHOULD_BE_HANDLED) > 0){
                Log.d("SoapForwardingHub", "Exception thrown up the stack");
                try{
                    Utilities.attemptToParseException((Exception) returnMessage.getMessage(), streamToRequestor);
                }catch(IllegalArgumentException e){
                    Log.e("SoapForwardingHub.acceptNetMessage", "Error not parseable, the error can not be a wsdl-specified one.");
                    return new InternalMessage(STATUS_FAULT | STATUS_FAULT_INVALID_PAYLOAD, null);
                }catch(ClassCastException e){
                    Log.e("SoapForwardingHub.acceptNetMessage", "The returned exception is not a subclass of Exception.");
                    return new InternalMessage(STATUS_FAULT | STATUS_FAULT_INVALID_PAYLOAD, null);
                }
            }
            Log.d("SoapForwardingHub", "Something went wrong, returning error");
            return returnMessage;
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

        if((message.statusCode & STATUS_HAS_MESSAGE) > 0){
            /* Easy if it already is an inputstream */
            if((message.statusCode & STATUS_MESSAGE_IS_INPUTSTREAM) > 0) {
                try{
                    InputStream messageAsStream = (InputStream)messageContent;
                    message.setMessage(messageAsStream);
                    return _server.sendMessage(message);
                }catch(ClassCastException e){
                    e.printStackTrace();
                    Log.e("SoapForwardingHub", "Someone set the RETURNING_MESSAGE_IS_INPUTSTREAM when in fact it wasn't.");
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
                message.statusCode = STATUS_OK|STATUS_HAS_MESSAGE|STATUS_MESSAGE_IS_INPUTSTREAM;
                return _server.sendMessage(message);
            }
        /* We have no content, must be a pure request */
        }else{
            return _server.sendMessage(message);
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
        return _server.getURI();
    }

    /**
     * Looks for the matching recipient to an endpoint input.
     * @param endpoint
     * @return
     */
    public ServiceConnection findRecipient(String endpoint){
        if(endpoint == null || endpoint.equals(""))
            return null;

        for(ServiceConnection connection : _services){

            // Ensure we have connection with endpoint
            if (connection == null || connection.getServiceEndpoint() == null)
                continue;

            if(endpoint.matches("^/?" + connection.getServiceEndpoint().replaceAll("^"+getInetAdress(), "") +"(.*)?")){
                return connection;
            }
        }
        return null;
    }
}
