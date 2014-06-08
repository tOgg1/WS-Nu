//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionChangedListener;
import org.ntnunotif.wsnu.services.filterhandling.FilterSupport;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.general.WebService;
import org.ntnunotif.wsnu.services.implementations.subscriptionmanager.AbstractSubscriptionManager;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;

import javax.jws.WebMethod;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Created by tormod on 3/11/14.
 */
public abstract class AbstractNotificationProducer extends WebService implements NotificationProducer, SubscriptionChangedListener {

    protected Notify currentMessage;
    protected NuNamespaceContextResolver currentMessageNamespaceContextResolver;
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
    public String generateSubscriptionKey(){
        Long time = System.nanoTime();
        String string = time.toString();
        String hash = "";
        try {
            hash = ServiceUtilities.generateSHA1Key(string);
            while(keyExists(hash))
                hash = ServiceUtilities.generateSHA1Key(string);
        } catch (NoSuchAlgorithmException e) {
            hash = ServiceUtilities.generateNTSHKey(string);
            while (keyExists(hash))
                hash = ServiceUtilities.generateNTSHKey(string);
        }
        return hash;
    }

    @WebMethod(exclude = true)
    public String generateNewHashedURL(String prefix) {
        String newHash = generateSubscriptionKey();

        String endpointReference = usesManager ? manager.getEndpointReference() : this.getEndpointReference();
        return endpointReference + "/?" + prefix +"=" + newHash;
    }

    @WebMethod(exclude = true)
    public String generateHashedURLFromKey(String prefix, String key) {
        String endpointReference = usesManager ? manager.getEndpointReference() : this.getEndpointReference();
        return endpointReference + "/?" + prefix + "=" + key;
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
     * @param namespaceContextResolver the {@link org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver} of the {@link org.oasis_open.docs.wsn.b_2.Notify}
     * @return the filtered {@link org.oasis_open.docs.wsn.b_2.Notify} element to send to this recipient, or
     * <code>null</code> if no message should be sent
     */
    @WebMethod(exclude = true)
    protected abstract Notify getRecipientFilteredNotify(String recipient, Notify notify, NuNamespaceContextResolver namespaceContextResolver);

    /**
     * Takes in a subscription key and returns the endpoint reference of this subscription.
     *
     * @return The endpoint reference, or null if the subscription does not exist.
     */
    @WebMethod(exclude = true)
    protected abstract String getEndpointReferenceOfRecipient(String subscriptionKey);

    /**
     * Will try to send the {@link org.oasis_open.docs.wsn.b_2.Notify} to the
     * {@link javax.xml.ws.wsaddressing.W3CEndpointReference} indicated.
     *
     * @param notify               the {@link org.oasis_open.docs.wsn.b_2.Notify} to send
     * @param w3CEndpointReference the reference of the receiving endpoint
     * @throws IllegalAccessException
     */
    @WebMethod(exclude = true)
    public void sendSingleNotify(Notify notify, W3CEndpointReference w3CEndpointReference) throws IllegalAccessException {
        if (hub == null) {
            Log.e("AbstractNotificationProducer", "Tried to send message with hub null. If a quickBuild is available," +
                    " consider running this before sending messages");
            return;
        }

        Log.d("AbstractNotificationProducer", "Was told to send single notify to a target");
        InternalMessage outMessage = new InternalMessage(STATUS_OK | STATUS_HAS_MESSAGE | STATUS_ENDPOINTREF_IS_SET, notify);
        outMessage.getRequestInformation().setEndpointReference(ServiceUtilities.getAddress(w3CEndpointReference));
        Log.d("AbstractNotificationProducer", "Forwarding Notify");
        hub.acceptLocalMessage(outMessage);
    }

    /**
     * Sends a notification to the endpoints. NamespaceContext is the context of the Notification.
     *
     * @param notify           the {@link org.oasis_open.docs.wsn.b_2.Notify} to send
     * @param namespaceContextResolver the {@link org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver} of the notify
     */
    @WebMethod(exclude = true)
    public void sendNotification(Notify notify, NuNamespaceContextResolver namespaceContextResolver) {
        ObjectFactory factory = new ObjectFactory();

        // bind namespaces to topics
        for (NotificationMessageHolderType holderType : notify.getNotificationMessage()) {

            TopicExpressionType topic = holderType.getTopic();

            if (holderType.getTopic() != null) {
                NuNamespaceContextResolver.NuResolvedNamespaceContext context = namespaceContextResolver.resolveNamespaceContext(topic);

                if (context == null) {
                    continue;
                }

                for (String prefix : context.getAllPrefixes()) {
                    // check if this is the default xmlns attribute
                    if (!prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                        // add namespace context to the expression node
                        topic.getOtherAttributes().put(new QName("xmlns:" + prefix), context.getNamespaceURI(prefix));
                    }
                }
            }
        }

        if (hub == null) {
            Log.e("AbstractNotificationProducer", "Tried to send message with hub null. If a quickBuild is available," +
                    " consider running this before sending messages");
            return;
        }

        // Remember current message with context
        currentMessage = notify;
        currentMessageNamespaceContextResolver = namespaceContextResolver;

        // For all valid recipients
        for (String recipient : this.getAllRecipients()) {

            // Filter do filter handling, if any
            Notify toSend = getRecipientFilteredNotify(recipient, notify, namespaceContextResolver);

            // If any message was left to send, send it
            if (toSend != null) {
                InternalMessage outMessage = new InternalMessage(STATUS_OK | STATUS_HAS_MESSAGE | STATUS_ENDPOINTREF_IS_SET, toSend);
                outMessage.getRequestInformation().setEndpointReference(getEndpointReferenceOfRecipient(recipient));
                hub.acceptLocalMessage(outMessage);
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
        sendNotification(notify, new NuNamespaceContextResolver());
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
                internalMessage.getRequestInformation().getNamespaceContextResolver());
    }

    @WebMethod(exclude = true)
    public void setSubscriptionManager(AbstractSubscriptionManager manager) {
        this.manager = manager;
        this.manager.addSubscriptionChangedListener(this);
        this.usesManager = true;
    }

    @WebMethod(exclude = true)
    public void clearSubscriptionManager() {
        if (manager != null) {
            this.manager.removeSubscriptionChangedListener(this);
        }
        this.manager = null;
        this.usesManager = false;
    }

    @WebMethod(exclude = true)
    public boolean usesSubscriptionManager() {
        return this.usesManager;
    }

    public static class SubscriptionHandle {
        public final ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple;
        public final FilterSupport.SubscriptionInfo subscriptionInfo;
        public boolean isPaused = false;

        public SubscriptionHandle(ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple,
                                  FilterSupport.SubscriptionInfo subscriptionInfo) {
            this.endpointTerminationTuple = endpointTerminationTuple;
            this.subscriptionInfo = subscriptionInfo;
        }
    }
}