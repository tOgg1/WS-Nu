package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.topics.TopicValidator;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionEvent;
import org.ntnunotif.wsnu.services.filterhandling.FilterSupport;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
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
 * Created by Inge on 31.03.2014.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "NotificationProducer")
public class GenericNotificationProducer extends AbstractNotificationProducer {

    private static final QName topicExpressionQName = new QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpression", "wsnt");

    private final Map<String, NotificationMessageHolderType>  latestMessages = new HashMap<>();

    private final FilterSupport filterSupport;

    private final boolean cacheMessages;

    private Map<String, SubscriptionHandle> subscriptions = new HashMap<>();

    public GenericNotificationProducer() {
        Log.d("GenericNotificationProducer", "Created new with default filter support and GetCurrentMessage allowed");
        filterSupport = FilterSupport.createDefaultFilterSupport();
        cacheMessages = true;
    }

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

    public GenericNotificationProducer(FilterSupport filterSupport, boolean cacheMessages) {
        if (cacheMessages)
            Log.d("GenericNotificationProducer", "Created new with custom filter support and GetCurrentMessage allowed");
        else
            Log.d("GenericNotificationProducer", "Created new with custom filter support and GetCurrentMessage disallowed");
        this.filterSupport = filterSupport;

        this.cacheMessages = cacheMessages;
    }

    public GenericNotificationProducer(Hub hub) {
        this._hub = hub;
        Log.d("GenericNotificationProducer", "Created new with hub, default filter support and GetCurrentMessage allowed");
        filterSupport = FilterSupport.createDefaultFilterSupport();
        cacheMessages = true;
    }

    public GenericNotificationProducer(Hub hub, boolean supportFilters) {
        this._hub = hub;
        if (supportFilters) {
            Log.d("GenericNotificationProducer", "Created new with hub, default filter support and GetCurrentMessage allowed");
            filterSupport = FilterSupport.createDefaultFilterSupport();
        } else {
            Log.d("GenericNotificationProducer", "Created new with hub and without filter support and GetCurrentMessage allowed");
            filterSupport = null;
        }
        cacheMessages = true;
    }

    public GenericNotificationProducer(Hub hub, boolean supportFilters, boolean cacheMessages) {
        this._hub = hub;
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

    public GenericNotificationProducer(Hub hub, FilterSupport filterSupport, boolean cacheMessages) {
        this._hub = hub;
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
            ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple = subscriptions.get(key).endpointTerminationTuple;
            if (endpointTerminationTuple.termination < System.currentTimeMillis()) {
                Log.d("SimpleNotificationProducer", "A subscription has been deemed too old: " + key);
                removeKeyList.add(key);
            }
        }

        // Remove keys
        for (String key: removeKeyList)
            subscriptions.remove(key);

        ArrayList<String> returnList = new ArrayList<>();

        // Filter out the paused subscriptions
        for (Map.Entry<String, SubscriptionHandle> entry : subscriptions.entrySet()) {
            if(!entry.getValue().isPaused){
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
        //NamespaceContext namespaceContext = _connection.getRequestInformation().getNamespaceContext();
        NuNamespaceContextResolver namespaceContextResolver = _connection.getRequestInformation().getNamespaceContextResolver();

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
        String subscriptionEndpoint = generateHashedURLFromKey("subscription", newSubscriptionKey);

        /* Build endpoint reference */
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(getEndpointReference() + "" + subscriptionEndpoint);

        response.setSubscriptionReference(builder.build());
        try {

        /* Set up the subscription */
            // create subscription info
            FilterSupport.SubscriptionInfo subscriptionInfo = new FilterSupport.SubscriptionInfo(filtersPresent,
                    namespaceContextResolver);

        ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple;

        endpointTerminationTuple = new ServiceUtilities.EndpointTerminationTuple(endpointReference, terminationTime);
        subscriptions.put(newSubscriptionKey, new SubscriptionHandle(endpointTerminationTuple, subscriptionInfo));

        if(usesManager){
            manager.addSubscriber(newSubscriptionKey, terminationTime);
        }

        Log.d("GenericNotificationProducer", "Added new subscription[" + newSubscriptionKey + "]: " + endpointReference);
        }catch(Exception e){
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

        //if (filterSupport == null || filterSupport.getFilterEvaluator(topicExpressionQName).is)

        // Find out which topic there was asked for (Exceptions automatically thrown)
        TopicExpressionType askedFor = getCurrentMessageRequest.getTopic();
        List<QName> topicQNames = TopicValidator.evaluateTopicExpressionToQName(askedFor, _connection.getRequestInformation().getNamespaceContext(askedFor));
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
    @WebMethod(exclude = true)
    public SoapForwardingHub quickBuild(String endpointReference) {
        try {
            // Ensure the application server is stopped.
            ApplicationServer.getInstance().stop();

            SoapForwardingHub hub = new SoapForwardingHub();
            _hub = hub;

            this.setEndpointReference(endpointReference);

            // Start the application server with this hub
            ApplicationServer.getInstance().start(hub);

            //* This is the most reasonable connector for this NotificationProducer *//*
            UnpackingConnector connector = new UnpackingConnector(this);
            hub.registerService(connector);
            _connection = connector;

            return hub;
        } catch (Exception e) {
            throw new RuntimeException("Unable to quickbuild: " + e.getMessage());
        }
    }

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
                return;
            default:
            case RENEW:
                return;
        }
    }
}
