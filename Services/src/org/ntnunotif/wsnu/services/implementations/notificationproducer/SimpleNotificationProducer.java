package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Simple Notification Producer, stores subscriptions in a HashMap,
 * and does not use a subscriptionmanger for subscriptionmanagement.
 *
 * @author Tormod Haugland
 *         Created by tormod on 23.03.14.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "NotificationProducer")
public class SimpleNotificationProducer extends AbstractNotificationProducer {

    private HashMap<String, ServiceUtilities.EndpointTerminationTuple> _subscriptions;

    /**
     * Constructor taking a hub as the reference
     * @param hub
     */
    public SimpleNotificationProducer(Hub hub) {
        super(hub);
        _subscriptions = new HashMap<>();
    }

    /**
     * Default constructor.
     */
    public SimpleNotificationProducer(){
        super();
        _subscriptions = new HashMap<>();
    }

    /**
     * Returns true if the subscriptions hashmap has the key
     * @param key
     * @return
     */
    @Override
    @WebMethod(exclude = true)
    public boolean keyExists(String key) {
        return _subscriptions.containsKey(key);
    }

    @Override
    @WebMethod(exclude = true)
    protected Collection<String> getAllRecipients() {
        // Something to remember which ones should be filtered out
        ArrayList<String> removeKeyList = new ArrayList<>();

        // go through all recipients and remember which should be removed
        for (String key : _subscriptions.keySet()) {
            ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple = _subscriptions.get(key);
            if (endpointTerminationTuple.termination < System.currentTimeMillis()) {
                Log.d("SimpleNotificationProducer", "A subscription has been deemed too old: " + key);
                removeKeyList.add(key);
            }
        }

        // Remove keys
        for (String key: removeKeyList)
            _subscriptions.remove(key);

        return _subscriptions.keySet();
    }

    @Override
    @WebMethod(exclude = true)
    protected Notify getRecipientFilteredNotify(String recipient, Notify notify, NamespaceContext namespaceContext) {
        return notify;
    }

    /**
     *
     * @param subscribeRequest
     * @return
     * @throws NotifyMessageNotSupportedFault
     * @throws UnrecognizedPolicyRequestFault
     * @throws TopicExpressionDialectUnknownFault
     * @throws ResourceUnknownFault
     * @throws InvalidTopicExpressionFault
     * @throws UnsupportedPolicyRequestFault
     * @throws InvalidFilterFault
     * @throws InvalidProducerPropertiesExpressionFault
     * @throws UnacceptableInitialTerminationTimeFault
     * @throws SubscribeCreationFailedFault
     * @throws TopicNotSupportedFault
     * @throws InvalidMessageContentExpressionFault
     */
    @Override
    @WebMethod(operationName = "Subscribe")
    public SubscribeResponse subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe",
                                                 targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Subscribe
                                                 subscribeRequest) throws
                                                 NotifyMessageNotSupportedFault, UnrecognizedPolicyRequestFault,
                                                 TopicExpressionDialectUnknownFault, ResourceUnknownFault,
                                                 InvalidTopicExpressionFault, UnsupportedPolicyRequestFault,
                                                 InvalidFilterFault, InvalidProducerPropertiesExpressionFault, UnacceptableInitialTerminationTimeFault,
                                                 SubscribeCreationFailedFault, TopicNotSupportedFault, InvalidMessageContentExpressionFault {
        Log.d("SimpleNotificationProducer", "Got new subscription request");

        W3CEndpointReference consumerEndpoint = subscribeRequest.getConsumerReference();

        if(consumerEndpoint == null){
            ServiceUtilities.throwSubscribeCreationFailedFault("Missing endpointreference");
        }

        String endpointReference = null;
        try {
            endpointReference = ServiceUtilities.getAddress(consumerEndpoint);
        } catch (IllegalAccessException e) {
            ServiceUtilities.throwSubscribeCreationFailedFault("EndpointReference malformatted or missing.");
        }

        FilterType filter = subscribeRequest.getFilter();

        if(filter != null){
            ServiceUtilities.throwInvalidFilterFault("NULL", "Filters not supported by this producer", new QName("null", "null"));
        }

        long terminationTime = 0;
        if(subscribeRequest.getInitialTerminationTime() != null){
            try {
                System.out.println(subscribeRequest.getInitialTerminationTime().getValue());
                terminationTime = ServiceUtilities.interpretTerminationTime(subscribeRequest.getInitialTerminationTime().getValue());

                if(terminationTime < System.currentTimeMillis()){
                    ServiceUtilities.throwUnacceptableInitialTerminationTimeFault("Termination time can not be before 'now'");
                }

            } catch (UnacceptableTerminationTimeFault unacceptableTerminationTimeFault) {
                ServiceUtilities.throwUnacceptableInitialTerminationTimeFault("Malformated termination time");
            }
        }else{
            /* Set it to terminate in one day */
            terminationTime = System.currentTimeMillis() + 86400*1000;
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
            ServiceUtilities.throwUnacceptableInitialTerminationTimeFault("Internal error: The date was not convertable to a gregorian calendar-instance. If the problem persists," +
                                                                          "please post an issue at http://github.com/tOgg1/WS-Nu");
        }

        /* Generate new subscription hash */
        String newSubscriptionKey = generateSubscriptionKey();
        String subscriptionEndpoint = generateSubscriptionURL(newSubscriptionKey);

        /* Build endpoint reference */
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(getEndpointReference() +""+ subscriptionEndpoint);

        response.setSubscriptionReference(builder.build());

        /* Set up the subscription */
        _subscriptions.put(newSubscriptionKey, new ServiceUtilities.EndpointTerminationTuple(endpointReference, terminationTime));
        Log.d("SimpleNotificationProducer", "Added new subscription[" + newSubscriptionKey +"]: " + endpointReference);

        return response;
    }

    /**
     * Returns the message stored by {@link #AbstractNotificationProducer}.
     * @param getCurrentMessageRequest
     * @return
     * @throws InvalidTopicExpressionFault
     * @throws TopicExpressionDialectUnknownFault
     * @throws MultipleTopicsSpecifiedFault
     * @throws ResourceUnknownFault
     * @throws NoCurrentMessageOnTopicFault
     * @throws TopicNotSupportedFault
     */
    @Override
    @WebResult(name = "GetCurrentMessageResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "GetCurrentMessageResponse")
    @WebMethod(operationName = "GetCurrentMessage")
    public GetCurrentMessageResponse getCurrentMessage(@WebParam(partName = "GetCurrentMessageRequest", name = "GetCurrentMessage",
                                                                 targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                                                       GetCurrentMessage getCurrentMessageRequest)
                                                       throws InvalidTopicExpressionFault, TopicExpressionDialectUnknownFault,
                                                       MultipleTopicsSpecifiedFault, ResourceUnknownFault, NoCurrentMessageOnTopicFault,
                                                       TopicNotSupportedFault {
        GetCurrentMessageResponse response = new GetCurrentMessageResponse();
        response.getAny().add(currentMessage);
        return response;
    }

    @Override
    @WebMethod(exclude = true)
    public SoapForwardingHub quickBuild() {
        try {
            SoapForwardingHub hub = new SoapForwardingHub();
            //* This is the most reasonable connector for this NotificationProducer *//*
            UnpackingConnector connector = new UnpackingConnector(this);
            hub.registerService(connector);
            _connection = connector;
            _hub = hub;
            return hub;
        } catch (Exception e) {
            throw new RuntimeException("Unable to quickbuild: " + e.getMessage());
        }
    }
}
