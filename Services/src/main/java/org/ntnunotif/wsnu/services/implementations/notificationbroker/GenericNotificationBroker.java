package org.ntnunotif.wsnu.services.implementations.notificationbroker;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.topics.TopicValidator;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionEvent;
import org.ntnunotif.wsnu.services.filterhandling.FilterSupport;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationFailedFault;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationRejectedFault;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.*;

/**
 * Created by tormod on 06.04.14.
 */
public class GenericNotificationBroker extends AbstractNotificationBroker {

    protected Map<String, SubscriptionHandle> subscriptions = new HashMap<>();
    protected Map<String, PublisherHandle> publishers = new HashMap<>();

    private final Map<String, NotificationMessageHolderType>  latestMessages = new HashMap<>();

    private final FilterSupport filterSupport;

    public GenericNotificationBroker() {
        super();

        filterSupport = FilterSupport.createDefaultFilterSupport();

        cacheMessages = true;

        demandRegistered = true;
    }



    @Override
    @WebMethod(exclude = true)
    public boolean keyExists(String key) {
        return subscriptions.containsKey(key);
    }

    @Override
    @WebMethod(exclude = true)
    protected Collection<String> getAllRecipients() {
        // Something to remember which ones should be filtered out
        ArrayList<String> removeKeyList = new ArrayList<>();

        // Go through all recipients and remember which should be removed
        for (String key : subscriptions.keySet()) {
            ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple = subscriptions.get(key).endpointTerminationTuple;
            if (endpointTerminationTuple.termination < System.currentTimeMillis()) {
                Log.d("SimpleNotificationProducer", "A subscription has been deemed too old: " + key);
                removeKeyList.add(key);
            }
        }

        // Remove keys
        for (String key: removeKeyList)
            subscriptions.remove(key);

        return subscriptions.keySet();
    }

    @Override
    @WebMethod(exclude = true)
    protected Notify getRecipientFilteredNotify(String recipient, Notify notify, NamespaceContext namespaceContext) {

        // See if we have the current recipient registered, and if message is cached
        if (!subscriptions.containsKey(recipient))
            return null;

        if (filterSupport == null)
            return notify;

        // Find current recipient to Notify
        SubscriptionHandle subscriptionHandle = subscriptions.get(recipient);

        // Delegate filtering to filter support
        return filterSupport.evaluateNotifyToSubscription(notify, subscriptionHandle.subscriptionInfo, namespaceContext);
    }

    @Override
    @WebMethod(exclude = true)
    public void sendNotification(Notify notify, NamespaceContext namespaceContext) {

        // Check if we should cache message
        if (cacheMessages) {
            // Take out the latest messages
            for (NotificationMessageHolderType messageHolderType : notify.getNotificationMessage()) {
                TopicExpressionType topic = messageHolderType.getTopic();

                // If it is connected to a topic, remember it
                if (topic != null) {

                    try {

                        List<QName> topicQNames = TopicValidator.evaluateTopicExpressionToQName(topic, namespaceContext);
                        String topicName = TopicUtils.topicToString(topicQNames);
                        latestMessages.put(topicName, messageHolderType);

                    } catch (InvalidTopicExpressionFault invalidTopicExpressionFault) {
                        Log.w("GenericNotificationBroker", "Tried to send a topic with an invalid expression");
                        invalidTopicExpressionFault.printStackTrace();
                    } catch (MultipleTopicsSpecifiedFault multipleTopicsSpecifiedFault) {
                        Log.w("GenericNotificationBroker", "Tried to send a message with multiple topics");
                        multipleTopicsSpecifiedFault.printStackTrace();
                    } catch (TopicExpressionDialectUnknownFault topicExpressionDialectUnknownFault) {
                        Log.w("GenericNotificationBroker", "Tried to send a topic with an invalid expression dialect");
                        topicExpressionDialectUnknownFault.printStackTrace();
                    }
                }
            }
        }
        // Super type can do the rest
        super.sendNotification(notify, namespaceContext);
    }

    @Override
    @WebMethod(operationName = "Subscribe")
    public SubscribeResponse subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe",
            targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Subscribe subscribeRequest)
            throws NotifyMessageNotSupportedFault, UnrecognizedPolicyRequestFault, TopicExpressionDialectUnknownFault,
            ResourceUnknownFault, InvalidTopicExpressionFault, UnsupportedPolicyRequestFault, InvalidFilterFault,
            InvalidProducerPropertiesExpressionFault, UnacceptableInitialTerminationTimeFault,
            SubscribeCreationFailedFault, TopicNotSupportedFault, InvalidMessageContentExpressionFault {

        // Log subscribe event
        Log.d("GenericNotificationBroker", "Got new subscription request");

        // Remember the namespace context
        NamespaceContext namespaceContext = _connection.getRequestInformation().getNamespaceContext();

        W3CEndpointReference consumerEndpoint = subscribeRequest.getConsumerReference();

        if (consumerEndpoint == null) {
            ServiceUtilities.throwSubscribeCreationFailedFault("en", "Missing endpointreference");
        }

        String endpointReference = null;
        try {
            endpointReference = ServiceUtilities.getAddress(consumerEndpoint);
        } catch (IllegalAccessException e) {
            ServiceUtilities.throwSubscribeCreationFailedFault("en", "EndpointReference mal formatted or missing.");
        }

        FilterType filters = subscribeRequest.getFilter();

        Map<QName, Object> filtersPresent = null;

        if (filters != null) {
            filtersPresent = new HashMap<>();

            for (Object o : filters.getAny()) {

                if (o instanceof JAXBElement) {
                    JAXBElement filter = (JAXBElement) o;

                    // Filter legality checks
                    if (filterSupport != null &&
                            filterSupport.supportsFilter(filter.getName(), filter.getValue(), namespaceContext)) {

                        QName fName = filter.getName();

                        Log.d("GenericNotificationBroker", "Subscription request contained filter: "
                                + fName);

                        filtersPresent.put(fName, filter.getValue());
                    } else {
                        Log.w("GenericNotificationBroker", "Subscription attempt with non-supported filter: "
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
                System.out.println(subscribeRequest.getInitialTerminationTime().getValue());
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
        String subscriptionEndpoint = generateSubscriptionURL(newSubscriptionKey);

        /* Build endpoint reference */
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(getEndpointReference() + "" + subscriptionEndpoint);

        response.setSubscriptionReference(builder.build());

        /* Set up the subscription */
        // create subscription info
        FilterSupport.SubscriptionInfo subscriptionInfo = new FilterSupport.SubscriptionInfo(filtersPresent,
                namespaceContext);
        ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple;
        endpointTerminationTuple = new ServiceUtilities.EndpointTerminationTuple(endpointReference, terminationTime);
        subscriptions.put(newSubscriptionKey, new SubscriptionHandle(endpointTerminationTuple, subscriptionInfo));

        Log.d("GenericNotificationBroker", "Added new subscription[" + newSubscriptionKey + "]: " + endpointReference);

        return response;
    }


    @Override
    @Oneway
    @WebMethod(operationName = "Notify")
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                       Notify notify) {
        this.sendNotification(notify);
    }

    /**
     * Register a publisher. This implementation does not take into account topics, and will never throw TopicNotSupportededFault.
     * @param registerPublisherRequest
     * @return
     * @throws InvalidTopicExpressionFault
     * @throws PublisherRegistrationFailedFault
     * @throws ResourceUnknownFault
     * @throws PublisherRegistrationRejectedFault
     * @throws UnacceptableInitialTerminationTimeFault
     * @throws TopicNotSupportedFault
     */
    @Override
    @WebResult(name = "RegisterPublisherResponse", targetNamespace = "http://docs.oasis-open.org/wsn/br-2", partName = "RegisterPublisherResponse")
    @WebMethod(operationName = "RegisterPublisher")
    public RegisterPublisherResponse registerPublisher(
            @WebParam(partName = "RegisterPublisherRequest",name = "RegisterPublisher", targetNamespace = "http://docs.oasis-open.org/wsn/br-2")
            RegisterPublisher registerPublisherRequest)
            throws InvalidTopicExpressionFault, PublisherRegistrationFailedFault, ResourceUnknownFault, PublisherRegistrationRejectedFault,
            UnacceptableInitialTerminationTimeFault, TopicNotSupportedFault {

        NamespaceContext namespaceContext = _connection.getRequestInformation().getNamespaceContext();

        W3CEndpointReference publisherEndpoint = registerPublisherRequest.getPublisherReference();

        if(publisherEndpoint == null){
            ServiceUtilities.throwPublisherRegistrationFailedFault("en", "Missing endpointreference");
        }

        String endpointReference = null;
        try {
            endpointReference = ServiceUtilities.getAddress(registerPublisherRequest.getPublisherReference());
        } catch (IllegalAccessException e) {
            ServiceUtilities.throwPublisherRegistrationFailedFault("en", "Could not register publisher, failed to " +
                    "understand the endpoint reference");
        }

        List<TopicExpressionType> topics = registerPublisherRequest.getTopic();

        for (TopicExpressionType topic : topics) {
            try {
                if(!TopicValidator.isLegalExpression(topic, namespaceContext)){
                    ServiceUtilities.throwTopicNotSupportedFault("en", "Expression given is not a legal topicexpression");
                }
            } catch (TopicExpressionDialectUnknownFault topicExpressionDialectUnknownFault) {
                topicExpressionDialectUnknownFault.printStackTrace();
            }
        }

        long terminationTime = registerPublisherRequest.getInitialTerminationTime().toGregorianCalendar().getTimeInMillis();

        if(terminationTime < System.currentTimeMillis()){
            ServiceUtilities.throwUnacceptableInitialTerminationTimeFault("en", "Invalid termination time. Can't be before current time");
        }

        String newSubscriptionKey = generateSubscriptionKey();
        String subscriptionEndpoint = generateSubscriptionURL(newSubscriptionKey);

        // Send subscriptionRequest back if isDemand isRequested
        if(registerPublisherRequest.isDemand()){
            this.sendSubscriptionRequest(endpointReference);
        }

        publishers.put(newSubscriptionKey,
                new PublisherHandle(new ServiceUtilities.EndpointTerminationTuple(newSubscriptionKey, terminationTime),
                                    topics, registerPublisherRequest.isDemand()));

        RegisterPublisherResponse response = new RegisterPublisherResponse();

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(getEndpointReference() +""+ subscriptionEndpoint);

        response.setConsumerReference(builder.build());
        response.setPublisherRegistrationReference(publisherEndpoint);
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
            Log.w("GenericNotificationBroker", "Someone tried to get current message when caching is disabled");
            ServiceUtilities.throwNoCurrentMessageOnTopicFault("en", "No messages are stored on Topic " +
                    getCurrentMessageRequest.getTopic().getContent());
        }

        // Find out which topic there was asked for (Exceptions automatically thrown)
        TopicExpressionType askedFor = getCurrentMessageRequest.getTopic();
        List<QName> topicQNames = TopicValidator.evaluateTopicExpressionToQName(askedFor, _connection.getRequestInformation().getNamespaceContext());
        String topicName = TopicUtils.topicToString(topicQNames);

        // Find latest message on this topic
        NotificationMessageHolderType holderType = latestMessages.get(topicName);

        if (holderType == null) {
            Log.d("GenericNotificationBroker", "Was asked for current message on a topic that was not sent");
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
    public SoapForwardingHub quickBuild(String endpointReference) {
        try {
            // Ensure the application server is stopped.
            ApplicationServer.getInstance().stop();

            SoapForwardingHub hub = new SoapForwardingHub();
            _hub = hub;

            this.setEndpointReference(endpointReference);

            // Start the application server with this hub
            ApplicationServer.getInstance().start(hub);

            //* This is the most reasonable connector for this NotificationBroker *//*
            UnpackingConnector connector = new UnpackingConnector(this);
            hub.registerService(connector);
            _connection = connector;

            return hub;
        } catch (Exception e) {
            throw new RuntimeException("Unable to quickbuild: " + e.getMessage());
        }
    }

    @WebMethod(exclude = true)
    public void pauseDemandPublishers(){
        for (Map.Entry<String, PublisherHandle> entry : publishers.entrySet()) {
            if(entry.getValue().demand){
                sendPauseRequest(entry.getKey());
            }
        }
    }

    @WebMethod(exclude = true)
    public void resumeDemandPublishers(){
        for (Map.Entry<String, PublisherHandle> entry : publishers.entrySet()) {
            if(entry.getValue().demand){
                sendResumeRequest(entry.getKey());
            }
        }
    }

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
}