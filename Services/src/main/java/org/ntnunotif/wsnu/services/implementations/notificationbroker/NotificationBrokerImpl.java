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

package org.ntnunotif.wsnu.services.implementations.notificationbroker;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.topics.TopicValidator;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.eventhandling.PublisherRegistrationEvent;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionEvent;
import org.ntnunotif.wsnu.services.filterhandling.FilterSupport;
import org.ntnunotif.wsnu.services.general.ExceptionUtilities;
import org.ntnunotif.wsnu.services.general.HelperClasses;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.general.WsnUtilities;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationFailedFault;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationRejectedFault;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.*;

/**
 * The generic NotificationBroker implementation.
 *
 * Implements all aspects of the NotificiationBroker-specification through the WS-Nu base.
 * This implementation stores subscriptions and publishers in HashMaps.
 *
 * @see {@link org.ntnunotif.wsnu.services.implementations.notificationbroker.AbstractNotificationBroker}.
 *
 * Created by tormod on 06.04.14.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/brw-2", name = "NotificationBroker")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class NotificationBrokerImpl extends AbstractNotificationBroker {

    /**
     * HashMap of subscriptions.
     */
    protected final Map<String, SubscriptionHandle> subscriptions = new HashMap<>();

    /**
     * HashMap of publishers.
     */
    protected final Map<String, PublisherHandle> publishers = new HashMap<>();

    /**
     * HashMap of latestMessages.
     */
    private final Map<String, NotificationMessageHolderType>  latestMessages = new HashMap<>();

    /**
     * FilterSupport variable.
     */
    private FilterSupport filterSupport;

    /**
     * Variable indicating whether the broker should cache messages. Setting this to true allows retrieval through
     * {@link org.oasis_open.docs.wsn.b_2.GetCurrentMessage}.
     */
    private boolean cacheMessages;

    /**
     * Default constructor. Adds filtersupport and enables caching of messages.
     */
    public NotificationBrokerImpl() {
        Log.d("NotificationBrokerImpl", "Created new with default filter support and GetCurrentMessage allowed");
        filterSupport = FilterSupport.createDefaultFilterSupport();
        cacheMessages = true;
    }


    /**
     * Constructor taking a hub as a parameter, sending it up to {@link org.ntnunotif.wsnu.services.implementations.notificationbroker.AbstractNotificationBroker}.
     * Also creates default filtersupport and sets cachemessages to true.
     * @param hub
     */
    public NotificationBrokerImpl(Hub hub) {
        this.hub = hub;
        Log.d("NotificationBrokerImpl", "Created new with hub, default filter support and GetCurrentMessage allowed");
        filterSupport = FilterSupport.createDefaultFilterSupport();
        cacheMessages = true;
    }

    /**
     * @return The current filtersupport.
     */
    public FilterSupport getFilterSupport() {
        return filterSupport;
    }

    /**
     * @return True if this producer is currently caching messages, else false.
     */
    public boolean cachesMessages() {
        return cacheMessages;
    }

    /**
     * Sets the filtersupport.
     * @param filterSupport A {@link org.ntnunotif.wsnu.services.filterhandling.FilterSupport} object.
     */
    public void setFilterSupport(FilterSupport filterSupport) {
        this.filterSupport = filterSupport;
    }

    /**
     * Set's whether or not the producer should cache messages.
     * @param cacheMessages
     */
    public void setCacheMessages(boolean cacheMessages) {
        this.cacheMessages = cacheMessages;
    }

    /**
     * Checks if the subscription-key is already registered with this broker. This is the case if either
     * the broker has a subscription or a publisher with the key. This is to ensure no duplicates.
     * @param key The key. Usually this would be a ntsh or SHA-1 hash.
     * @return
     */
    @Override
    @WebMethod(exclude = true)
    public boolean keyExists(String key) {
        return subscriptions.containsKey(key) || publishers.containsKey(key);
    }

    /**
     * Get all subscriptions that are eligible to receive notifications.
     * </p>
     * This method will also remove expired subscriptions.
     * @return A collection of endpoint references.
     */
    @Override
    @WebMethod(exclude = true)
    protected Collection<String> getAllRecipients() {
        // Something to remember which ones should be filtered out
        ArrayList<String> removeKeyList = new ArrayList<>();

        // Go through all recipients and remember which should be removed
        for (String key : subscriptions.keySet()) {
            HelperClasses.EndpointTerminationTuple endpointTerminationTuple = subscriptions.get(key).endpointTerminationTuple;
            if (endpointTerminationTuple.termination < System.currentTimeMillis()) {
                Log.d("SimpleNotificationProducer", "A subscription has been deemed too old: " + key);
                removeKeyList.add(key);
            }
        }

        // Remove keys
        for (String key : removeKeyList) {
            subscriptions.remove(key);
        }

        return subscriptions.keySet();
    }


    @Override
    protected String getEndpointReferenceOfRecipient(String subscriptionKey) {
        return subscriptions.get(subscriptionKey).endpointTerminationTuple.endpoint;
    }

    /**
     * Takes a notify
     * @param recipient        the recipient to ask
     * @param notify           the {@link org.oasis_open.docs.wsn.b_2.Notify} that should be filtered for sending
     * @param namespaceContextResolver the {@link org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver} of the {@link org.oasis_open.docs.wsn.b_2.Notify}
     * @return
     */
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

    /**
     * Sends a notification.
     * @param notify The {@link org.oasis_open.docs.wsn.b_2.Notify} to send
     * @param namespaceContextResolver the {@link org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver} of the notify
     */
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
                        Log.w("NotificationBrokerImpl", "Tried to send a topic with an invalid expression");
                        invalidTopicExpressionFault.printStackTrace();
                    } catch (MultipleTopicsSpecifiedFault multipleTopicsSpecifiedFault) {
                        Log.w("NotificationBrokerImpl", "Tried to send a message with multiple topics");
                        multipleTopicsSpecifiedFault.printStackTrace();
                    } catch (TopicExpressionDialectUnknownFault topicExpressionDialectUnknownFault) {
                        Log.w("NotificationBrokerImpl", "Tried to send a topic with an invalid expression dialect");
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
        Log.d("NotificationBrokerImpl", "Got new subscription request");

        // Remember the namespace context
        //NamespaceContext namespaceContextResolver = connection.getRequestInformation().getNamespaceContext();

        W3CEndpointReference consumerEndpoint = subscribeRequest.getConsumerReference();

        if (consumerEndpoint == null) {
            ExceptionUtilities.throwSubscribeCreationFailedFault("en", "Missing endpointreference");
        }

        String endpointReference = ServiceUtilities.getAddress(consumerEndpoint);

        // EndpointReference is returned as "" from getAddress if something went wrong.
        if(endpointReference.equals("")){
            ExceptionUtilities.throwSubscribeCreationFailedFault("en", "EndpointReference mal formatted or missing.");
        }

        Log.initLogFile();
        Log.setWriteToFile(true);
        FilterType filters = subscribeRequest.getFilter();

        Map<QName, Object> filtersPresent = null;

        if (filters != null) {
            filtersPresent = new HashMap<>();

            for (Object o : filters.getAny()) {

                if (o instanceof JAXBElement) {
                    JAXBElement filter = (JAXBElement) o;

                    // Get the na,espace context for this filter
                    NamespaceContext namespaceContext = connection.getRequestInformation().getNamespaceContext(filter.getValue());

                    // Filter legality checks
                    if (filterSupport != null &&
                            filterSupport.supportsFilter(filter.getName(), filter.getValue(), namespaceContext)) {

                        QName fName = filter.getName();

                        Log.d("NotificationBrokerImpl", "Subscription request contained filter: "
                                + fName);

                        filtersPresent.put(fName, filter.getValue());
                    } else {
                        Log.w("NotificationBrokerImpl", "Subscription attempt with non-supported filter: "
                                + filter.getName());
                        ExceptionUtilities.throwInvalidFilterFault("en", "Filter not supported for this producer: " +
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
                    ExceptionUtilities.throwUnacceptableInitialTerminationTimeFault("en", "Termination time can not be before 'now'");
                }

            } catch (UnacceptableTerminationTimeFault unacceptableTerminationTimeFault) {
                ExceptionUtilities.throwUnacceptableInitialTerminationTimeFault("en", "Malformated termination time");
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
            ExceptionUtilities.throwUnacceptableInitialTerminationTimeFault("en", "Internal error: The date was not " +
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

        /* Set up the subscription */
        // create subscription info
        FilterSupport.SubscriptionInfo subscriptionInfo = new FilterSupport.SubscriptionInfo(filtersPresent,
                connection.getRequestInformation().getNamespaceContextResolver());
        HelperClasses.EndpointTerminationTuple endpointTerminationTuple;
        endpointTerminationTuple = new HelperClasses.EndpointTerminationTuple(endpointReference, terminationTime);
        subscriptions.put(newSubscriptionKey, new SubscriptionHandle(endpointTerminationTuple, subscriptionInfo));

        Log.d("NotificationBrokerImpl", "Added new subscription[" + newSubscriptionKey + "]: " + endpointReference);

        return response;
    }

    /**
     * Implementation of the NotificationBroker's notify. This method does nothing but forward the notify by calling
     * {@link #sendNotification(org.oasis_open.docs.wsn.b_2.Notify)}
     * @param notify The Notify object.
     */
    @Override
    @Oneway
    @WebMethod(operationName = "Notify")
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                       Notify notify) {
        this.sendNotification(notify);
    }

    /**
     * An implementation of the WS-N specification's RegisterPublisher.
     *
     * The implementation is designed to conform fully to the specification. Any specific can be found at
     *
     * <href>http://docs.oasis-open.org/wsn/wsn-ws_brokered_notification-1.3-spec-os.htm#_Toc133294203</href>
     *
     * @param registerPublisherRequest The register publisher object
     * @return A RegisterPublisherResponse containing the endpoint of the registration.
     * @throws InvalidTopicExpressionFault If topic expression dialect is unknown.
     * @throws PublisherRegistrationFailedFault This can be thrown in a number of circumstances. If any general internal error occurs, this is thrown.
     * @throws ResourceUnknownFault Never thrown as of version 0.4.
     * @throws PublisherRegistrationRejectedFault Never thrown. A {@link org.oasis_open.docs.wsn.brw_2.PublisherRegistrationFailedFault} is always thrown instead.
     * @throws UnacceptableInitialTerminationTimeFault If the termination time was invalid
     * @throws TopicNotSupportedFault If the Topic given is not a supported topic expression.
     */
    @Override
    @WebResult(name = "RegisterPublisherResponse", targetNamespace = "http://docs.oasis-open.org/wsn/br-2", partName = "RegisterPublisherResponse")
    @WebMethod(operationName = "RegisterPublisher")
    public RegisterPublisherResponse registerPublisher(
            @WebParam(partName = "RegisterPublisherRequest",name = "RegisterPublisher", targetNamespace = "http://docs.oasis-open.org/wsn/br-2")
            RegisterPublisher registerPublisherRequest)
            throws InvalidTopicExpressionFault, PublisherRegistrationFailedFault, ResourceUnknownFault, PublisherRegistrationRejectedFault,
            UnacceptableInitialTerminationTimeFault, TopicNotSupportedFault {

        //NamespaceContext namespaceContext = connection.getRequestInformation().getNamespaceContext();
        NuNamespaceContextResolver namespaceContextResolver = connection.getRequestInformation().getNamespaceContextResolver();

        W3CEndpointReference publisherEndpoint = registerPublisherRequest.getPublisherReference();

        if(publisherEndpoint == null){
            ExceptionUtilities.throwPublisherRegistrationFailedFault("en", "Missing endpointreference");
        }

        String endpointReference = ServiceUtilities.getAddress(registerPublisherRequest.getPublisherReference());

        // EndpointReference is returned as "" from getAddress if something went wrong.
        if(endpointReference.equals("")){
            ExceptionUtilities.throwPublisherRegistrationFailedFault("en", "Could not register publisher, failed to " +
                    "understand the endpoint reference");
        }

        List<TopicExpressionType> topics = registerPublisherRequest.getTopic();

        for (TopicExpressionType topic : topics) {
            try {
                if(!TopicValidator.isLegalExpression(topic, namespaceContextResolver.resolveNamespaceContext(topic))){
                    ExceptionUtilities.throwTopicNotSupportedFault("en", "Expression given is not a legal topicexpression");
                }
            } catch (TopicExpressionDialectUnknownFault topicExpressionDialectUnknownFault) {
                ExceptionUtilities.throwInvalidTopicExpressionFault("en", "TopicExpressionDialect unknown");
            }
        }

        long terminationTime = registerPublisherRequest.getInitialTerminationTime().toGregorianCalendar().getTimeInMillis();

        if(terminationTime < System.currentTimeMillis()){
            ExceptionUtilities.throwUnacceptableInitialTerminationTimeFault("en", "Invalid termination time. Can't be before current time");
        }

        String newSubscriptionKey = generateSubscriptionKey();
        String subscriptionEndpoint = generateHashedURLFromKey(WsnUtilities.publisherRegistrationString, newSubscriptionKey);

        // Send subscriptionRequest back if isDemand isRequested
        if(registerPublisherRequest.isDemand()){
            WsnUtilities.sendSubscriptionRequest(endpointReference, getEndpointReference(), getHub());
        }

        publishers.put(newSubscriptionKey,
                new PublisherHandle(new HelperClasses.EndpointTerminationTuple(newSubscriptionKey, terminationTime),
                                    topics, registerPublisherRequest.isDemand()));

        RegisterPublisherResponse response = new RegisterPublisherResponse();

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(subscriptionEndpoint);

        response.setConsumerReference(builder.build());
        response.setPublisherRegistrationReference(publisherEndpoint);
        return response;
    }

    /**
     * Implementation of {@link org.oasis_open.docs.wsn.b_2.GetCurrentMessage}.
     *
     * This message will always fault unless {@link #cacheMessages} is true.
     *
     * @param getCurrentMessageRequest The request object
     * @return A {@link org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse} object with the latest message on the request topic.
     * @throws InvalidTopicExpressionFault Thrown either if the topic is invalid, or if no topic is given.
     * @throws TopicExpressionDialectUnknownFault Thrown if the topic expression uses a dialect not known
     * @throws MultipleTopicsSpecifiedFault Never thrown due to the nature of the {@link org.oasis_open.docs.wsn.b_2.GetCurrentMessage} object.
     * @throws ResourceUnknownFault Never thrown as of version 0.4, as WS-Resources is not implemented.
     * @throws NoCurrentMessageOnTopicFault If no message is listed on the current topic.
     * @throws TopicNotSupportedFault Never thrown as of version 0.3.
     */
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
            Log.w("NotificationBrokerImpl", "Someone tried to get current message when caching is disabled");
            ExceptionUtilities.throwNoCurrentMessageOnTopicFault("en", "No messages are stored on Topic " +
                    getCurrentMessageRequest.getTopic().getContent());
        }

        Log.d("NotificationBrokerImpl", "Accepted getCurrentMessage");
        // Find out which topic there was asked for (Exceptions automatically thrown)
        TopicExpressionType askedFor = getCurrentMessageRequest.getTopic();

        if(askedFor == null) {
            ExceptionUtilities.throwInvalidTopicExpressionFault("en", "Topic missing from request.");
        }

        List<QName> topicQNames = TopicValidator.evaluateTopicExpressionToQName(askedFor, connection.getRequestInformation().getNamespaceContext(askedFor));

        String topicName = TopicUtils.topicToString(topicQNames);

        // Find latest message on this topic
        NotificationMessageHolderType holderType = latestMessages.get(topicName);

        if (holderType == null) {
            Log.d("NotificationBrokerImpl", "Was asked for current message on a topic that was not sent");
            ExceptionUtilities.throwNoCurrentMessageOnTopicFault("en", "There was no messages on the topic requested");
            return null;
        } else {
            GetCurrentMessageResponse response = new GetCurrentMessageResponse();
            // TODO check out if this should be the content of the response
            response.getAny().add(holderType.getMessage());
            return response;
        }
    }

    /**
     * If we have any publisher that uses demand-based publishing, this method will be called when we have no subscriptions
     * and need to send a pause-request to the relevant publishers.
     */
    @WebMethod(exclude = true)
    public void pauseDemandPublishers(){
        for (Map.Entry<String, PublisherHandle> entry : publishers.entrySet()) {
            if(entry.getValue().demand){
                WsnUtilities.sendPauseRequest(entry.getKey(), getHub());
            }
        }
    }

    /**
     * If we have any publisher that uses demand-based publishing, this method will be called when we have gone from having no subscriptions.
     * to having one or more subscriptions, and need to continue our publishers.
     */
    @WebMethod(exclude = true)
    public void resumeDemandPublishers(){
        for (Map.Entry<String, PublisherHandle> entry : publishers.entrySet()) {
            if(entry.getValue().demand){
                WsnUtilities.sendResumeRequest(entry.getKey(), getHub());
            }
        }
    }

    /**
     * Method implemented through {@link org.ntnunotif.wsnu.services.eventhandling.SubscriptionChangedListener}.
     * This method performs relevant tasks when a subscription is changed, such as removing subscriptions and pause
     * demand-based publishers.
     * @param event
     */
    @Override
    @WebMethod(exclude=true)
    public void subscriptionChanged(SubscriptionEvent event) {
        SubscriptionHandle handle;
        switch(event.getType()){
            case PAUSE:
                handle = subscriptions.get(event.getSubscriptionReference());
                if(handle != null){
                    handle.isPaused = true;
                }
                return;
            case RESUME:
                handle = subscriptions.get(event.getSubscriptionReference());
                if(handle != null){
                    handle.isPaused = false;
                }
                return;
            case UNSUBSCRIBE:
                subscriptions.remove(event.getSubscriptionReference());

                //If we have any demand-based publishers we need to pause our subscription if we dont have any subscriptions left here
                if(subscriptions.size() == 0){
                    pauseDemandPublishers();
                }
                return;
            default:
            case RENEW:
                return;
        }
    }

    /**
     * Method implemented through {@link org.ntnunotif.wsnu.services.eventhandling.PublisherRegistrationEvent}.
     * This method performs relevant tasks when a publisher registration is changed. Currently this is only removing publishers
     * on destruction.
     * @param event The event-object containing information regarding the actual change.
     */
    @Override
    @WebMethod(exclude = true)
    public void publisherChanged(PublisherRegistrationEvent event) {
        switch(event.getType()){
            case DESTROYED:
                publishers.remove(event.getRegistrationReference());
        }
    }
}
