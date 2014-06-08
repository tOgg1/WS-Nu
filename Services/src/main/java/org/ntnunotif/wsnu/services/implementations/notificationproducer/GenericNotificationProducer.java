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
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.topics.TopicValidator;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionEvent;
import org.ntnunotif.wsnu.services.filterhandling.FilterSupport;
import org.ntnunotif.wsnu.services.general.HelperClasses;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.general.WsnUtilities;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.*;

/**
 * A <code>GenericNotificationProducer</code> has the ability to register subscriptions with filters. It can cache the
 * latest messages it has sent (one message per topic). It can be configured to do all this, just one thing or none.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "NotificationProducer")
public class GenericNotificationProducer extends AbstractNotificationProducer {

    private static final QName topicExpressionQName = new QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpression", "wsnt");

    private final Map<String, NotificationMessageHolderType> latestMessages = new HashMap<>();

    private final FilterSupport filterSupport;

    private final boolean cacheMessages;

    private final Map<String, SubscriptionHandle> subscriptions = new HashMap<>();

    /**
     * Creates a <code>GenericNotificationProducer</code> which caches messages and has default filter support (filter
     * on topic and message content).
     */
    public GenericNotificationProducer() {
        Log.d("GenericNotificationProducer", "Created new with default filter support and GetCurrentMessage allowed");
        filterSupport = FilterSupport.createDefaultFilterSupport();
        cacheMessages = true;
    }

    /**
     * Creates a <code>GenericNotificationProducer</code> which caches messages and may have default filter support
     * (filter on topic and message content).
     *
     * @param supportFilters if this producer should have default filter support.
     */
    public GenericNotificationProducer(boolean supportFilters) {
        if (supportFilters) {
            Log.d("GenericNotificationProducer", "Created new with default filter support and GetCurrentMessage allowed");
            filterSupport = FilterSupport.createDefaultFilterSupport();
        } else {
            Log.d("GenericNotificationProducer", "Created new without filter support and GetCurrentMessage allowed");
            filterSupport = null;
        }
        cacheMessages = true;
    }

    /**
     * Creates a <code>GenericNotificationProducer</code> which may cache messages and may have default filter support
     * (filter on topic and message content).
     *
     * @param supportFilters if this producer should have default filter support.
     * @param cacheMessages  if this producer should cache the latest messages on a topic
     */
    public GenericNotificationProducer(boolean supportFilters, boolean cacheMessages) {
        if (supportFilters) {
            if (cacheMessages) {
                Log.d("GenericNotificationProducer", "Created new with default filter support and GetCurrentMessage allowed");
                filterSupport = FilterSupport.createDefaultFilterSupport();
            } else {
                Log.d("GenericNotificationProducer", "Created new with default filter support and GetCurrentMessage disallowed");
                filterSupport = FilterSupport.createDefaultFilterSupport();
            }
        } else {
            if (cacheMessages) {
                Log.d("GenericNotificationProducer", "Created new without filter support and GetCurrentMessage allowed, but unusable");
                filterSupport = null;
            } else {
                Log.d("GenericNotificationProducer", "Created new without filter support and GetCurrentMessage disallowed");
                filterSupport = null;
            }
        }
        this.cacheMessages = cacheMessages;
    }

    /**
     * Creates a <code>GenericNotificationProducer</code> which may cache messages and supports the filters
     * the {@link org.ntnunotif.wsnu.services.filterhandling.FilterSupport} supports.
     *
     * @param filterSupport the {@link org.ntnunotif.wsnu.services.filterhandling.FilterSupport} to use with filters
     * @param cacheMessages if this producer should cache the latest messages on a topic
     */
    public GenericNotificationProducer(FilterSupport filterSupport, boolean cacheMessages) {
        if (cacheMessages)
            Log.d("GenericNotificationProducer", "Created new with custom filter support and GetCurrentMessage allowed");
        else
            Log.d("GenericNotificationProducer", "Created new with custom filter support and GetCurrentMessage disallowed");
        this.filterSupport = filterSupport;

        this.cacheMessages = cacheMessages;
    }

    /**
     * Creates a <code>GenericNotificationProducer</code> which caches messages and has default filter support (filter
     * on topic and message content).
     *
     * @param hub the hub this producer should be connected to after startup
     */
    public GenericNotificationProducer(Hub hub) {
        this.hub = hub;
        Log.d("GenericNotificationProducer", "Created new with hub, default filter support and GetCurrentMessage allowed");
        filterSupport = FilterSupport.createDefaultFilterSupport();
        cacheMessages = true;
    }

    /**
     * Creates a <code>GenericNotificationProducer</code> which caches messages and may have default filter support
     * (filter on topic and message content).
     *
     * @param hub            the hub this producer should be connected to after startup
     * @param supportFilters if this producer should have default filter support.
     */
    public GenericNotificationProducer(Hub hub, boolean supportFilters) {
        this.hub = hub;
        if (supportFilters) {
            Log.d("GenericNotificationProducer", "Created new with hub, default filter support and GetCurrentMessage allowed");
            filterSupport = FilterSupport.createDefaultFilterSupport();
        } else {
            Log.d("GenericNotificationProducer", "Created new with hub and without filter support and GetCurrentMessage allowed");
            filterSupport = null;
        }
        cacheMessages = true;
    }

    /**
     * Creates a <code>GenericNotificationProducer</code> which may cache messages and may have default filter support
     * (filter on topic and message content).
     *
     * @param hub            the hub this producer should be connected to after startup
     * @param supportFilters if this producer should have default filter support.
     * @param cacheMessages  if this producer should cache the latest messages on a topic
     */
    public GenericNotificationProducer(Hub hub, boolean supportFilters, boolean cacheMessages) {
        this.hub = hub;
        if (supportFilters) {
            if (cacheMessages) {
                Log.d("GenericNotificationProducer", "Created new with hub, default filter support and GetCurrentMessage allowed");
                filterSupport = FilterSupport.createDefaultFilterSupport();
            } else {
                Log.d("GenericNotificationProducer", "Created new with hub, default filter support and GetCurrentMessage disallowed");
                filterSupport = FilterSupport.createDefaultFilterSupport();
            }
        } else {
            if (cacheMessages) {
                Log.d("GenericNotificationProducer", "Created new with hub, without filter support and GetCurrentMessage allowed, but unusable");
                filterSupport = null;
            } else {
                Log.d("GenericNotificationProducer", "Created new with hub, without filter support and GetCurrentMessage disallowed");
                filterSupport = null;
            }
        }
        this.cacheMessages = cacheMessages;
    }

    /**
     * Creates a <code>GenericNotificationProducer</code> which may cache messages and supports the filters
     * the {@link org.ntnunotif.wsnu.services.filterhandling.FilterSupport} supports.
     *
     * @param hub           the hub this producer should be connected to after startup
     * @param filterSupport the {@link org.ntnunotif.wsnu.services.filterhandling.FilterSupport} to use with filters
     * @param cacheMessages if this producer should cache the latest messages on a topic
     */
    public GenericNotificationProducer(Hub hub, FilterSupport filterSupport, boolean cacheMessages) {
        this.hub = hub;
        if (cacheMessages)
            Log.d("GenericNotificationProducer", "Created new with hub, custom filter support and GetCurrentMessage allowed");
        else
            Log.d("GenericNotificationProducer", "Created new with hub, custom filter support and GetCurrentMessage disallowed");
        this.filterSupport = filterSupport;

        this.cacheMessages = cacheMessages;
    }

    @Override
    @WebMethod(exclude = true)
    public boolean keyExists(String key) {
        return subscriptions.containsKey(key);
    }

    @Override
    @WebMethod(exclude = true)
    protected final Collection<String> getAllRecipients() {
        // Something to remember which ones should be filtered out
        ArrayList<String> removeKeyList = new ArrayList<>();

        // go through all recipients and remember which should be removed
        for (String key : subscriptions.keySet()) {
            HelperClasses.EndpointTerminationTuple endpointTerminationTuple = subscriptions.get(key).endpointTerminationTuple;
            if (endpointTerminationTuple.termination < System.currentTimeMillis()) {
                Log.d("SimpleNotificationProducer", "A subscription has been deemed too old: " + key);
                removeKeyList.add(key);
            }
        }

        // Remove keys
        for (String key : removeKeyList)
            subscriptions.remove(key);

        ArrayList<String> returnList = new ArrayList<>();

        // Filter out the paused subscriptions
        for (Map.Entry<String, SubscriptionHandle> entry : subscriptions.entrySet()) {
            if (!entry.getValue().isPaused) {
                returnList.add(entry.getKey());
            }
        }
        return returnList;
    }

    @Override
    protected String getEndpointReferenceOfRecipient(String subscriptionKey) {
        return subscriptions.get(subscriptionKey).endpointTerminationTuple.endpoint;
    }


    @Override
    @WebMethod(exclude = true)
    protected Notify getRecipientFilteredNotify(String recipient, Notify notify, NuNamespaceContextResolver namespaceContextResolver) {

        // See if we have the current recipient registered, and if message is cached
        if (!subscriptions.containsKey(recipient))
            return null;

        if (filterSupport == null)
            return notify;

        // Find current recipient to Notify
        SubscriptionHandle subscriptionHandle = subscriptions.get(recipient);

        // Delegate filtering to filter support
        return filterSupport.evaluateNotifyToSubscription(notify, subscriptionHandle.subscriptionInfo, namespaceContextResolver);
    }

    @Override
    @WebMethod(exclude = true)
    public void sendNotification(Notify notify, NuNamespaceContextResolver namespaceContextResolver) {

        // Check if we should cache message
        if (cacheMessages) {
            // Take out the latest messages
            for (NotificationMessageHolderType messageHolderType : notify.getNotificationMessage()) {
                TopicExpressionType topic = messageHolderType.getTopic();

                // If it is connected to a topic, remember it
                if (topic != null) {

                    try {

                        List<QName> topicQNames = TopicValidator.evaluateTopicExpressionToQName(topic, namespaceContextResolver.resolveNamespaceContext(topic));
                        String topicName = TopicUtils.topicToString(topicQNames);
                        latestMessages.put(topicName, messageHolderType);

                    } catch (InvalidTopicExpressionFault invalidTopicExpressionFault) {
                        Log.w("GenericNotificationProducer", "Tried to send a topic with an invalid expression");
                        invalidTopicExpressionFault.printStackTrace();
                    } catch (MultipleTopicsSpecifiedFault multipleTopicsSpecifiedFault) {
                        Log.w("GenericNotificationProducer", "Tried to send a message with multiple topics");
                        multipleTopicsSpecifiedFault.printStackTrace();
                    } catch (TopicExpressionDialectUnknownFault topicExpressionDialectUnknownFault) {
                        Log.w("GenericNotificationProducer", "Tried to send a topic with an invalid expression dialect");
                        topicExpressionDialectUnknownFault.printStackTrace();
                    }
                }
            }
        }
        // Super type can do the rest
        super.sendNotification(notify, namespaceContextResolver);
    }

    /**
     * The Subscribe request message as defined by the WS-N specification.
     *
     * More information can be found at <href>http://docs.oasis-open.org/wsn/wsn-ws_base_notification-1.3-spec-os.htm#_Toc133735624</href>
     * @param subscribeRequest A {@link org.oasis_open.docs.wsn.b_2.Subscribe} object.
     * @return A {@link org.oasis_open.docs.wsn.b_2.SubscribeResponse} if the subscription was added successfully.
     * @throws NotifyMessageNotSupportedFault Never.
     * @throws UnrecognizedPolicyRequestFault Never, policies will not be added until 2.0.
     * @throws TopicExpressionDialectUnknownFault  If the topic expression was not valid.
     * @throws ResourceUnknownFault Never, WS-Resources is not added as of 0.3
     * @throws InvalidTopicExpressionFault If any topic expression added was invalid.
     * @throws UnsupportedPolicyRequestFault Never, policies will not be added until 2.0
     * @throws InvalidFilterFault If the filter was invalid.
     * @throws InvalidProducerPropertiesExpressionFault Never.
     * @throws UnacceptableInitialTerminationTimeFault If the subscription termination time was invalid.
     * @throws SubscribeCreationFailedFault If any internal or general fault occured during the processing of a subscription request.
     * @throws TopicNotSupportedFault If the topic in some way is unknown or unsupported.
     * @throws InvalidMessageContentExpressionFault Never.
     */
    @Override
    @WebMethod(operationName = "Subscribe")
    public SubscribeResponse subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe",
            targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Subscribe subscribeRequest)
            throws NotifyMessageNotSupportedFault, UnrecognizedPolicyRequestFault, TopicExpressionDialectUnknownFault,
            ResourceUnknownFault, InvalidTopicExpressionFault, UnsupportedPolicyRequestFault, InvalidFilterFault,
            InvalidProducerPropertiesExpressionFault, UnacceptableInitialTerminationTimeFault,
            SubscribeCreationFailedFault, TopicNotSupportedFault, InvalidMessageContentExpressionFault {

        // Log subscribe event
        Log.d("GenericNotificationProducer", "Got new subscription request");

        // Remember the namespace context
        //NamespaceContext namespaceContext = connection.getRequestInformation().getNamespaceContext();
        NuNamespaceContextResolver namespaceContextResolver = connection.getRequestInformation().getNamespaceContextResolver();

        W3CEndpointReference consumerEndpoint = subscribeRequest.getConsumerReference();

        if (consumerEndpoint == null) {
            ServiceUtilities.throwSubscribeCreationFailedFault("en", "Missing endpointreference");
        }

        String endpointReference = ServiceUtilities.getAddress(consumerEndpoint);

        // EndpointReference is returned as "" from getAddress if something went wrong.
        if(endpointReference.equals("")){
            ServiceUtilities.throwSubscribeCreationFailedFault("en", "EndpointReference mal formatted or missing.");
        }

        FilterType filters = subscribeRequest.getFilter();

        Map<QName, Object> filtersPresent = null;

        if (filters != null) {
            filtersPresent = new HashMap<>();

            for (Object o : filters.getAny()) {

                if (o instanceof JAXBElement) {
                    JAXBElement filter = (JAXBElement) o;
                    Object fi = filter.getValue();

                    // Filter legality checks
                    if (filterSupport != null &&
                            filterSupport.supportsFilter(filter.getName(), fi, namespaceContextResolver.resolveNamespaceContext(fi))) {

                        QName fName = filter.getName();

                        Log.d("GenericNotificationProducer", "Subscription request contained filter: "
                                + fName);

                        filtersPresent.put(fName, filter.getValue());
                    } else {
                        Log.w("GenericNotificationProducer", "Subscription attempt with non-supported filter: "
                                + filter.getName());
                        ServiceUtilities.throwInvalidFilterFault("en", "Filter not supported for this producer: " +
                                filter.getName(), filter.getName());
                    }

                }
            }
        }

        long terminationTime = 0;

        if (subscribeRequest.getInitialTerminationTime() != null) {
            try {
                terminationTime = ServiceUtilities.interpretTerminationTime(subscribeRequest.getInitialTerminationTime().getValue());

                if (terminationTime < System.currentTimeMillis()) {
                    ServiceUtilities.throwUnacceptableInitialTerminationTimeFault("en", "Termination time can not be before 'now'");
                }

            } catch (UnacceptableTerminationTimeFault unacceptableTerminationTimeFault) {
                ServiceUtilities.throwUnacceptableInitialTerminationTimeFault("en", "Malformated termination time");
            }
        } else {
            /* Set it to terminate in one day */
            terminationTime = System.currentTimeMillis() + 86400 * 1000;
        }

        /* Generate the response */
        SubscribeResponse response = new SubscribeResponse();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(terminationTime);

        try {
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
            response.setTerminationTime(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.d("SimpleNotificationProducer", "Could not convert date time, is it formatted properly?");
            ServiceUtilities.throwUnacceptableInitialTerminationTimeFault("en", "Internal error: The date was not " +
                    "convertable to a gregorian calendar-instance. If the problem persists," +
                    "please post an issue at http://github.com/tOgg1/WS-Nu");
        }

        /* Generate new subscription hash */
        String newSubscriptionKey = generateSubscriptionKey();
        String subscriptionEndpoint = generateHashedURLFromKey(WsnUtilities.subscriptionString, newSubscriptionKey);

        /* Build endpoint reference */
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(subscriptionEndpoint);

        response.setSubscriptionReference(builder.build());
        try {

        /* Set up the subscription */
            // create subscription info
            FilterSupport.SubscriptionInfo subscriptionInfo = new FilterSupport.SubscriptionInfo(filtersPresent,
                    namespaceContextResolver);

            HelperClasses.EndpointTerminationTuple endpointTerminationTuple;

            endpointTerminationTuple = new HelperClasses.EndpointTerminationTuple(endpointReference, terminationTime);
            subscriptions.put(newSubscriptionKey, new SubscriptionHandle(endpointTerminationTuple, subscriptionInfo));

            if (usesManager) {
                manager.addSubscriber(newSubscriptionKey, terminationTime);
            }

            Log.d("GenericNotificationProducer", "Added new subscription[" + newSubscriptionKey + "]: " + endpointReference);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    @WebResult(name = "GetCurrentMessageResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2",
            partName = "GetCurrentMessageResponse")
    @WebMethod(operationName = "GetCurrentMessage")
    public GetCurrentMessageResponse getCurrentMessage(@WebParam(partName = "GetCurrentMessageRequest",
            name = "GetCurrentMessage", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") GetCurrentMessage
                                                               getCurrentMessageRequest) throws
            InvalidTopicExpressionFault, TopicExpressionDialectUnknownFault, MultipleTopicsSpecifiedFault,
            ResourceUnknownFault, NoCurrentMessageOnTopicFault, TopicNotSupportedFault {

        if (!cacheMessages) {
            Log.w("GenericNotificationProducer", "Someone tried to get current message when caching is disabled");
            ServiceUtilities.throwNoCurrentMessageOnTopicFault("en", "This producer does not cache messages, " +
                    "and therefore does not support the getCurrentMessage interface");
        }

        // Find out which topic there was asked for (Exceptions automatically thrown)
        TopicExpressionType askedFor = getCurrentMessageRequest.getTopic();

        if (askedFor == null) {
            ServiceUtilities.throwInvalidTopicExpressionFault("en", "Topic missing from request.");
        }

        List<QName> topicQNames = TopicValidator.evaluateTopicExpressionToQName(askedFor, connection.getRequestInformation().getNamespaceContext(askedFor));
        String topicName = TopicUtils.topicToString(topicQNames);

        // Find latest message on this topic
        NotificationMessageHolderType holderType = latestMessages.get(topicName);

        if (holderType == null) {
            Log.d("GenericNotificationProducer", "Was asked for current message on a topic that was not sent");
            ServiceUtilities.throwNoCurrentMessageOnTopicFault("en", "There was no messages on the topic requested");
            return null;

        } else {
            GetCurrentMessageResponse response = new GetCurrentMessageResponse();
            // TODO check out if this should be the content of the response
            response.getAny().add(holderType.getMessage());
            return response;
        }
    }

    @Override
    public void subscriptionChanged(SubscriptionEvent event) {
        SubscriptionHandle handle;
        switch (event.getType()) {
            case PAUSE:
                handle = subscriptions.get(event.getSubscriptionReference());
                if (handle != null) {
                    handle.isPaused = true;
                }
                return;
            case RESUME:
                handle = subscriptions.get(event.getSubscriptionReference());
                if (handle != null) {
                    handle.isPaused = false;
                }
                return;
            case UNSUBSCRIBE:
                subscriptions.remove(event.getSubscriptionReference());
                return;
            default:
            case RENEW:
                return;
        }
    }
}
