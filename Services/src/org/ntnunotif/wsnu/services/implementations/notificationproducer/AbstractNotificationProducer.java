package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.general.WebService;
import org.ntnunotif.wsnu.services.general.NotificationProducer;
import org.oasis_open.docs.wsn.b_2.*;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Created by tormod on 3/11/14.
 */
public abstract class AbstractNotificationProducer extends WebService implements NotificationProducer {

    protected Notify currentMessage;

    /**
     * Constructor taking a hub as a parameter.
     * @param hub
     */
    protected AbstractNotificationProducer(Hub hub) {
        super(hub);
    }

    /**
     * Default constructor.
     */
    protected AbstractNotificationProducer() {}

    /**
     * Generates a SHA-1 encryption key, or a NTSH key if SHA-1 is not found on the system
     * @return
     * @throws java.security.NoSuchAlgorithmException
     */
    public String generateSubscriptionKey(){
        Long time = System.nanoTime();
        String string = time.toString();
        String hash = "";
        try{
            hash = ServiceUtilities.generateSHA1Key(string);
            while(keyExists(hash))
                hash = ServiceUtilities.generateSHA1Key(string);
        }catch(NoSuchAlgorithmException e){
            hash = ServiceUtilities.generateNTSHKey(string);
            while(keyExists(hash))
                hash = ServiceUtilities.generateNTSHKey(string);
        }
        return hash;
    }

    public String generateNewSubscriptionURL(){
        String newHash = null;
        newHash = generateSubscriptionKey();
        String baseAddress = _hub.getInetAdress();

        return getEndpointReference() + "/?subscription=" + newHash;
    }

    public String generateSubscriptionURL(String key){
        String baseURI = _hub.getInetAdress();
        return baseURI + "/?subscription=" + key;
    }

    public abstract boolean keyExists(String key);

    public abstract List<String> getRecipients(Notify notify);

    /**
     * Sends a notification the the endpoint.
     * @param notify
     */
    public void sendNotification(Notify notify){
        currentMessage = notify;
        List<String> recipients = getRecipients(notify);
        for(String endPoint : recipients){
            InternalMessage outMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_ENDPOINTREF_IS_SET, notify);
            outMessage.getRequestInformation().setEndpointReference(endPoint);
            _hub.acceptLocalMessage(outMessage);
        }
    }

    /**
     * Attempts to send a notification taken as a string.
     * @param notify
     */
    public void sendNotification(String notify) throws JAXBException {
        InputStream iStream = new ByteArrayInputStream(notify.getBytes());
        this.sendNotification((Notify)XMLParser.parse(iStream).getMessage());
    }

    /**
     * Attempts to send a notification taken as an inputstream.
     * @param iStream
     * @throws JAXBException
     */
    public void sendNotification(InputStream iStream) throws JAXBException {
        this.sendNotification((Notify)XMLParser.parse(iStream).getMessage());
    }
}
