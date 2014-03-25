package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.services.general.WebService;
import org.ntnunotif.wsnu.services.general.NotificationProducer;
import org.oasis_open.docs.wsn.b_2.*;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Created by tormod on 3/11/14.
 */
public abstract class AbstractNotificationProducer extends WebService implements NotificationProducer {


    /**
     * Default and only constructor. This does not have to called if the hub is set
     * @param hub
     */
    protected AbstractNotificationProducer(Hub hub) {
        super(hub);
    }

    protected AbstractNotificationProducer() {}

    /**
     * Generates a SHA-1 encryption key
     * @return
     * @throws java.security.NoSuchAlgorithmException
     */
    public String generateSubscriptionKey(){
        Long time = System.nanoTime();
        String string = time.toString();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return
        }
        String hash = "";

        boolean first = false;

        while(keyExists(hash) || !first){
            first = true;
            byte[] bytes = digest.digest(string.getBytes());
            StringBuilder sb = new StringBuilder();

            for (int i=0; i < bytes.length; i++) {

                sb.append( Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16).substring( 1 ));
            }
            hash = sb.toString();
        }
        return hash;
    }

    public String generateNewSubscriptionURL(){
        String newHash = null;
        try {
            newHash = generateSubscriptionKey();
        /* We don't have SHA-1 available */
        } catch (NoSuchAlgorithmException e) {

        }
        String baseAddress = _hub.getInetAdress();

        return baseAddress + "/?subscription=" + newHash;
    }

    public String generateSubscriptionURL(String key){
        String baseURI = _hub.getInetAdress();
        return baseURI + "/?subscription=" + key;
    }

    public abstract boolean keyExists(String key);

    /**
     * Sends a notification the the endpoint.
     * @param notify
     * @param endPoint Endpoint of the recipient. Can be formatted as an IPv4, IPv6 or URL adress.
     */
    public void sendNotification(Notify notify, String endPoint){
        InternalMessage outMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_ENDPOINTREF_IS_SET, notify);
        outMessage.getRequestInformation().setEndpointReference(endPoint);
        _hub.acceptLocalMessage(new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, notify));
    }

    /**
     * Sends a notification the the endpoint.
     * @param notify
     * @param endPoint Endpoint of the recipient. Can be formatted as an IPv4, IPv6 or URL adress.
     */
    public void sendNotification(String notify, String endPoint){
        InternalMessage outMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_ENDPOINTREF_IS_SET, notify);
        outMessage.getRequestInformation().setEndpointReference(endPoint);
        _hub.acceptLocalMessage(new InternalMessage(STATUS_OK| STATUS_HAS_MESSAGE, notify));
    }

    /**
     * Sends a notification the the endpoint.
     * @param notify
     * @param endPoint Endpoint of the recipient. Can be formatted as an IPv4, IPv6 or URL adress.
     */
    public void sendNotification(InputStream notify, String endPoint){
        InternalMessage outMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_ENDPOINTREF_IS_SET|STATUS_MESSAGE_IS_INPUTSTREAM, notify);
        outMessage.getRequestInformation().setEndpointReference(endPoint);
        _hub.acceptLocalMessage(new InternalMessage(STATUS_OK| STATUS_HAS_MESSAGE, notify));
    }

    /**
     * Sends a notification the the endpoint.
     * @param notify
     * @param requestInformation Relevant requestInformation. EndpointReference contained in this object MUST be set.
     */
    public void sendNotification(Notify notify, RequestInformation requestInformation){
        InternalMessage outMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_ENDPOINTREF_IS_SET, notify);
        outMessage.setRequestInformation(requestInformation);
        _hub.acceptLocalMessage(new InternalMessage(STATUS_OK| STATUS_HAS_MESSAGE, notify));
    }

    /**
     * Sends a notification the the endpoint.
     * @param notify
     * @param requestInformation Relevant requestInformation. EndpointReference contained in this object MUST be set.
     */
    public void sendNotification(String notify, RequestInformation requestInformation){
        InternalMessage outMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_ENDPOINTREF_IS_SET, notify);
        outMessage.setRequestInformation(requestInformation);
        _hub.acceptLocalMessage(new InternalMessage(STATUS_OK| STATUS_HAS_MESSAGE, notify));
    }

    /**
     * Sends a notification the the endpoint.
     * @param notify
     * @param requestInformation Relevant requestInformation. EndpointReference contained in this object MUST be set.
     */
    public void sendNotification(InputStream notify, RequestInformation requestInformation){
        InternalMessage outMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_ENDPOINTREF_IS_SET|STATUS_MESSAGE_IS_INPUTSTREAM, notify);
        outMessage.setRequestInformation(requestInformation);
        _hub.acceptLocalMessage(new InternalMessage(STATUS_OK| STATUS_HAS_MESSAGE, notify));
    }
}
