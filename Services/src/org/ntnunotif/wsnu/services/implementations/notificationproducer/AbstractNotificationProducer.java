package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.net.NuNamespaceContext;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.general.WebService;
import org.ntnunotif.wsnu.services.implementations.subscriptionmanager.AbstractSubscriptionManager;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;

import javax.jws.WebMethod;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Created by tormod on 3/11/14.
 */
public abstract class AbstractNotificationProducer extends WebService implements NotificationProducer {

    protected Notify currentMessage;
    protected NamespaceContext currentMessageNamespaceContext;
    protected AbstractSubscriptionManager manager;
    protected boolean usesManager;

    /**
     * Constructor taking a hub as a parameter.
     *
     * @param hub
     */
    protected AbstractNotificationProducer(Hub hub) {
        super(hub);
    }

    /**
     * Default constructor.
     */
    protected AbstractNotificationProducer() {
    }

    /**
     * Generates a SHA-1 encryption key, or a NTSH key if SHA-1 is not found on the system
     *
     * @return
     * @throws java.security.NoSuchAlgorithmException
     */
    @WebMethod(exclude = true)
    public String generateSubscriptionKey() {
        Long time = System.nanoTime();
        String string = time.toString();
        String hash = "";
        try {
            hash = ServiceUtilities.generateSHA1Key(string);
            while (keyExists(hash))
                hash = ServiceUtilities.generateSHA1Key(string);
        } catch (NoSuchAlgorithmException e) {
            hash = ServiceUtilities.generateNTSHKey(string);
            while (keyExists(hash))
                hash = ServiceUtilities.generateNTSHKey(string);
        }
        return hash;
    }

    @WebMethod(exclude = true)
    public String generateNewSubscriptionURL() {
        String newHash = generateSubscriptionKey();

        String endpointReference = usesManager ? manager.getEndpointReference() : this.getEndpointReference();
        return endpointReference + "/?subscription=" + newHash;
    }

    @WebMethod(exclude = true)
    public String generateSubscriptionURL(String key) {
        String endpointReference = usesManager ? manager.getEndpointReference() : this.getEndpointReference();
        return endpointReference + "/?subscription=" + key;
    }

    @WebMethod(exclude = true)
    public abstract boolean keyExists(String key);

    /**
     * Should return a {@link java.util.Collection} with all recipients connected to the
     * <code>AbstractNotificationProducer</code> that are still valid.
     *
     * @return all recipients
     */
    @WebMethod(exclude = true)
    protected abstract Collection<String> getAllRecipients();

    /**
     * Should do any filter handling or equivalent for a {@link org.oasis_open.docs.wsn.b_2.Notify} for a given recipient
     *
     * @param recipient        the recipient to ask
     * @param notify           the {@link org.oasis_open.docs.wsn.b_2.Notify} that should be filtered for sending
     * @param namespaceContext the {@link javax.xml.namespace.NamespaceContext} of the {@link org.oasis_open.docs.wsn.b_2.Notify}
     * @return the filtered {@link org.oasis_open.docs.wsn.b_2.Notify} element to send to this recipient, or
     * <code>null</code> if no message should be sent
     */
    @WebMethod(exclude = true)
    protected abstract Notify getRecipientFilteredNotify(String recipient, Notify notify, NamespaceContext namespaceContext);

    /**
     * Sends a notification to the endpoints. NamespaceContext is the context of the Notification.
     *
     * @param notify           the {@link org.oasis_open.docs.wsn.b_2.Notify} to send
     * @param namespaceContext the {@link javax.xml.namespace.NamespaceContext} of the notify
     */
    @WebMethod(exclude = true)
    public void sendNotification(Notify notify, NamespaceContext namespaceContext) {

        // Remember current message with context
        currentMessage = notify;
        currentMessageNamespaceContext = namespaceContext;

        // For all valid recipients
        for (String recipient : this.getAllRecipients()) {

            // Filter do filter handling, if any
            Notify toSend = getRecipientFilteredNotify(recipient, notify, namespaceContext);

            // If any message was left to send, send it
            if (toSend != null) {
                InternalMessage outMessage = new InternalMessage(STATUS_OK | STATUS_HAS_MESSAGE | STATUS_ENDPOINTREF_IS_SET, toSend);
                outMessage.getRequestInformation().setEndpointReference(recipient);
                _hub.acceptLocalMessage(outMessage);
            }
        }
    }

    /**
     * Sends a notification the the endpoint.
     *
     * @param notify
     */
    @WebMethod(exclude = true)
    public void sendNotification(Notify notify) {
        sendNotification(notify, new NuNamespaceContext());
    }

    /**
     * Attempts to send a notification taken as a string.
     *
     * @param notify
     */
    @WebMethod(exclude = true)
    public void sendNotification(String notify) throws JAXBException {
        InputStream iStream = new ByteArrayInputStream(notify.getBytes());
        this.sendNotification(iStream);
    }

    /**
     * Attempts to send a notification taken as an inputstream.
     *
     * @param iStream
     * @throws JAXBException
     */
    @WebMethod(exclude = true)
    public void sendNotification(InputStream iStream) throws JAXBException {
        InternalMessage internalMessage = XMLParser.parse(iStream);
        this.sendNotification((Notify) internalMessage.getMessage(),
                internalMessage.getRequestInformation().getNamespaceContext());
    }

    @WebMethod(exclude = true)
    public void setSubscriptionManager(AbstractSubscriptionManager manager) {
        this.manager = manager;
        this.usesManager = true;
    }

    @WebMethod(exclude = true)
    public void clearSubscriptionManager() {
        this.manager = null;
        this.usesManager = false;
    }

    @WebMethod(exclude = true)
    public boolean usesSubscriptionManager() {
        return this.usesManager;
    }
}
