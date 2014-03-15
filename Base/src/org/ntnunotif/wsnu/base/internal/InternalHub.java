package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Utilities;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
        _server = ApplicationServer.getInstance();
        _services = new ArrayList<WebServiceConnection>();
        _server.start(this);
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
    public InternalMessage acceptNetMessage(InputStream inputStream){

        InternalMessage returnMessage = null;

        /* Decrypt message */
        InternalMessage parsedMessage;
        try {
            parsedMessage = XMLParser.parse(inputStream);
        } catch (JAXBException e) {
            //TODO: Move this handling to the parser?
            returnMessage = new InternalMessage(InternalMessage.STATUS_FAULT_INTERNAL_ERROR, null);
            e.printStackTrace();
            return returnMessage;
        }

        /* Try sending the message to everyone, perhaps do endpoint-reference testing here? */
        for(WebServiceConnection service : _services){

            /* Send the message forward */
            InternalMessage message = service.acceptMessage(parsedMessage);

            /* Incorrect destination */
            if((message.statusCode & InternalMessage.STATUS_INVALID_DESTINATION) > 0)
            {
                continue;
            }

            if((message.statusCode & InternalMessage.STATUS_OK) > 0){
                if((message.statusCode & InternalMessage.STATUS_HAS_RETURNING_MESSAGE) > 0){
                    /* This is easy, now we can convert it, and send it straight out*/
                    if((message.statusCode & InternalMessage.STATUS_RETURNING_MESSAGE_IS_OUTPUTSTREAM) > 0){
                        try{
                            InputStream returningStream = Utilities.convertToInputStream((OutputStream) message.getMessage());
                            returnMessage = new InternalMessage(InternalMessage.STATUS_OK
                                    | InternalMessage.STATUS_HAS_RETURNING_MESSAGE
                                    | InternalMessage.STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM, returningStream);
                            break;

                        }catch(ClassCastException e){
                            System.err.println("Someone set the RETURNING_MESSAGE_IS_OUTPUTSTREAM flag when the message in the InternalMessage in fact was not");
                            e.printStackTrace();
                        }
                    /* Even better, the stream is already an inputstream */
                    } else if((message.statusCode & InternalMessage.STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM) > 0){
                        try{
                            InputStream returningStream = (InputStream)message.getMessage();
                            returnMessage = new InternalMessage(InternalMessage.STATUS_OK
                                                       | InternalMessage.STATUS_HAS_RETURNING_MESSAGE
                                                       | InternalMessage.STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM, returningStream);
                            break;

                        }catch(ClassCastException e){
                            System.err.println("Someone set the RETURNING_MESSAGE_IS_INPUTSTREAM flag when the message in the InternalMessage in fact was not");
                            e.printStackTrace();
                        }
                    }

                    /* This is worse, now we have to find out what the payload is, and convert it to a stream*/
                    InputStream returningStream = Utilities.convertUnknownToInputStream(message);
                    
                    if(returningStream == null){
                        System.err.println("Someone set the HAS_RETURNING_MESSAGE flag when there was no returning mesasge.");
                        returnMessage = new InternalMessage(InternalMessage.STATUS_OK, null);
                        break;

                    }else{
                        returnMessage = new InternalMessage(InternalMessage.STATUS_OK
                                                   | InternalMessage.STATUS_HAS_RETURNING_MESSAGE
                                                   | InternalMessage.STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM, returningStream);
                        break;
                    }
                /* Everything is fine, and no message is to be returned */
                }else{
                    returnMessage = new InternalMessage(InternalMessage.STATUS_OK, null);
                }
            }else if((message.statusCode & InternalMessage.STATUS_FAULT) > 0){

                /* There is not specified any specific fault, so we treat it as a generic fault */
                if(message.statusCode == InternalMessage.STATUS_FAULT) {
                    returnMessage = new InternalMessage(message.statusCode, null);
                    break;

                }else if((message.statusCode & InternalMessage.STATUS_FAULT_INTERNAL_ERROR) > 0){
                    returnMessage = new InternalMessage(message.statusCode, null);
                    break;

                }else if((message.statusCode & InternalMessage.STATUS_FAULT_INVALID_PAYLOAD) > 0){
                    returnMessage = new InternalMessage(message.statusCode, null);
                    break;

                }else if((message.statusCode & InternalMessage.STATUS_FAULT_UNKNOWN_METHOD) > 0){
                    returnMessage = new InternalMessage(message.statusCode, null);
                    break;
                }else{
                    returnMessage = new InternalMessage(message.statusCode, null);
                    break;
                }
            /* Something weird is going on, neither OK or FAULT is flagged*/
            }else{
                returnMessage = new InternalMessage(InternalMessage.STATUS_FAULT, null);
                break;
            }
        }
        return returnMessage;
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
