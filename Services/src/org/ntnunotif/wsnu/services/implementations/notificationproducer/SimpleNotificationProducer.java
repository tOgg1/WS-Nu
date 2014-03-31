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
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

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

    /**
     *
     * @param notify
     * @return
     */
    @Override
    @WebMethod(exclude = true)
    public List<String> getRecipients(Notify notify) {
        ArrayList<String> recipients = new ArrayList<>();
        for (ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple : _subscriptions.values()) {
            recipients.add(endpointTerminationTuple.endpoint);
        }
        return recipients;
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
            throw new SubscribeCreationFailedFault("Missing EndpointReference");
        }

        //TODO: This is not particularly pretty, make WebService have a W3Cendpointreference variable instead of String?
        String endpointReference = ServiceUtilities.parseW3CEndpoint(consumerEndpoint.toString());

        FilterType filter = subscribeRequest.getFilter();

        if(filter != null){
            throw new InvalidFilterFault("Filters not supported for this NotificationProducer");
        }

        long terminationTime = 0;
        if(subscribeRequest.getInitialTerminationTime() != null){
            try {
                System.out.println(subscribeRequest.getInitialTerminationTime().getValue());
                terminationTime = ServiceUtilities.interpretTerminationTime(subscribeRequest.getInitialTerminationTime().getValue());

                if(terminationTime < System.currentTimeMillis()){
                    throw new UnacceptableInitialTerminationTimeFault();
                }

            } catch (UnacceptableTerminationTimeFault unacceptableTerminationTimeFault) {
                throw new UnacceptableInitialTerminationTimeFault();
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
            throw new UnacceptableInitialTerminationTimeFault();
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
        GetCurrentMessageResponse response = baseFactory.createGetCurrentMessageResponse();
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
