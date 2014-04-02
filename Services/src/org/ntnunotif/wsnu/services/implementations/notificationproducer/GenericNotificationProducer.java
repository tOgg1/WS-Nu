package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.util.Log;
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

    private final FilterSupport filterSupport;

    private Map<String, SubscriptionHandle> subscriptions;

    /**
     * Common code for ALL constructors
     */ {
        subscriptions = new HashMap<>();
    }

    public GenericNotificationProducer() {
        filterSupport = FilterSupport.createDefaultFilterSupport();
    }

    @Override
    @WebMethod(exclude = true)
    public boolean keyExists(String key) {
        return subscriptions.containsKey(key);
    }

    @Override
    @WebMethod(exclude = true)
    public List<String> getRecipients(Notify notify) {
        List<String> recipients = new ArrayList<>();

        for (SubscriptionHandle sub: subscriptions.values()) {
            if (filterSupport.evaluateNotifyToSubscription(notify, sub.subscriptionInfo)) {
                recipients.add(sub.endpointTerminationTuple.endpoint);
            }
        }

        return recipients;
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

        W3CEndpointReference consumerEndpoint = subscribeRequest.getConsumerReference();

        if (consumerEndpoint == null) {
            throw new SubscribeCreationFailedFault("Missing EndpointReference");
        }

        //TODO: This is not particularly pretty, make WebService have a W3Cendpointreference variable instead of String?
        String endpointReference = ServiceUtilities.parseW3CEndpoint(consumerEndpoint.toString());

        FilterType filters = subscribeRequest.getFilter();

        Map<QName, Object> filtersPresent = null;

        if (filters != null) {
            filtersPresent = new HashMap<>();

            for (Object o : filters.getAny()) {

                // TODO handle other class types, if they can exist:
                if (o instanceof JAXBElement) {
                    JAXBElement filter = (JAXBElement) o;

                    // Filter legality checks
                    if (filterSupport.supportsFilter(filter.getName())) {
                        QName fName = filter.getName();
                        Class fClass = filter.getDeclaredType();

                        if (fClass.equals(filterSupport.getFilterClass(fName))) {
                            Log.d("GenericNotificationProducer", "Subscription request contained filter: "
                                    + fName);

                            filtersPresent.put(fName, fClass);
                            filtersPresent.put(fName, filter.getValue());

                        } else {
                            Log.w("GenericNotificationProducer", "Subscription attempt with incorrect filter handle: "
                                    + fName + " Declared class: " + fClass + " Actual class: "
                                    + filterSupport.getFilterClass(fName));
                            throw new InvalidFilterFault("Filter was not translated correctly; evaluation failed");
                            // TODO Incorrect filter present
                        }
                    } else {
                        Log.w("GenericNotificationProducer", "Subscription attempt with non-supported filter: "
                                + filter.getName());
                        throw new InvalidFilterFault("Filter not supported for this producer");
                        // TODO Filter NOT supported
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
                    // TODO Create helper function to fill in fault.
                    throw new UnacceptableInitialTerminationTimeFault();
                }

            } catch (UnacceptableTerminationTimeFault unacceptableTerminationTimeFault) {
                // TODO check up on this
                throw new UnacceptableInitialTerminationTimeFault();
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
            // TODO Fill in fault
            throw new UnacceptableInitialTerminationTimeFault();
        }

        /* Generate new subscription hash */
        String newSubscriptionKey = generateSubscriptionKey();
        String subscriptionEndpoint = generateSubscriptionURL(newSubscriptionKey);

        /* Build endpoint reference */
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(getEndpointReference() + "" + subscriptionEndpoint);

        response.setSubscriptionReference(builder.build());

        /* Set up the subscription */
        // TODO create subscription info
        FilterSupport.SubscriptionInfo subscriptionInfo = new FilterSupport.SubscriptionInfo(filtersPresent);
        ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple;
        endpointTerminationTuple = new ServiceUtilities.EndpointTerminationTuple(endpointReference,terminationTime);
        subscriptions.put(newSubscriptionKey, new SubscriptionHandle(endpointTerminationTuple, subscriptionInfo));

        Log.d("GenericNotificationProducer", "Added new subscription[" + newSubscriptionKey + "]: " + endpointReference);

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

        return null;
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

    private static class SubscriptionHandle {
        final ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple;
        final FilterSupport.SubscriptionInfo subscriptionInfo;

        SubscriptionHandle(ServiceUtilities.EndpointTerminationTuple endpointTerminationTuple,
                           FilterSupport.SubscriptionInfo subscriptionInfo) {
            this.endpointTerminationTuple = endpointTerminationTuple;
            this.subscriptionInfo = subscriptionInfo;
        }
    }
}
