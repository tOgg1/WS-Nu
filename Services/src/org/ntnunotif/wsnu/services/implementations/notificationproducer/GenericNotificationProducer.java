package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.net.NuNamespaceContext;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
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
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

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
    public void sendNotification(Notify notify) {
        sendNotification(notify, new NuNamespaceContext());
    }

    @Override
    @WebMethod(exclude = true)
    public void sendNotification(String notify) throws JAXBException {
        InputStream iStream = new ByteArrayInputStream(notify.getBytes());
        this.sendNotification(iStream);
    }

    @Override
    @WebMethod(exclude = true)
    public void sendNotification(InputStream iStream) throws JAXBException {
        InternalMessage internalMessage = XMLParser.parse(iStream);
        this.sendNotification((Notify) internalMessage.getMessage(),
                internalMessage.getRequestInformation().getNamespaceContext());
    }

    @WebMethod(exclude = true)
    public void sendNotification(Notify notify, NamespaceContext namespaceContext) {
        currentMessage = notify;
        // To remember which subscriptions to remove
        List<String> keysToRemove = new ArrayList<>();

        // Find out which part of the Notify should be sent to each recipient
        for (String key : subscriptions.keySet()) {

            // Find current recipient to Notify
            SubscriptionHandle subscriptionHandle = subscriptions.get(key);

            // Should the subscription be removed?
            if (subscriptionHandle.endpointTerminationTuple.termination < System.currentTimeMillis()) {
                keysToRemove.add(key);
            } else {

                // Find out which parts get accepted through filters, if any
                Notify toSend = filterSupport.evaluateNotifyToSubscription(notify, subscriptionHandle.subscriptionInfo,
                        namespaceContext);

                // If something was left to send, wrap it in an InternalMessage and send it
                if (toSend != null) {

                    InternalMessage outMessage = new InternalMessage(STATUS_OK | STATUS_HAS_MESSAGE |
                            STATUS_ENDPOINTREF_IS_SET, toSend);
                    outMessage.getRequestInformation().
                            setEndpointReference(subscriptionHandle.endpointTerminationTuple.endpoint);
                    _hub.acceptLocalMessage(outMessage);
                }
            }
        }

        // Remove subscriptions that were outdated
        for (String key : keysToRemove)
            subscriptions.remove(key);
    }

    @Deprecated
    @Override
    @WebMethod(exclude = true)
    public List<String> getRecipients(Notify notify) {
        throw new UnsupportedOperationException("The getRecipients is no longer supported in " +
                "GenericNotificationProducer. It has been replaced by an override of sendNotification(Notify, " +
                "NamespaceContext)");
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
        NamespaceContext namespaceContext = _connection.getReqeustInformation().getNamespaceContext();

        W3CEndpointReference consumerEndpoint = subscribeRequest.getConsumerReference();

        if (consumerEndpoint == null) {
            ServiceUtilities.throwSubscribeCreationFailedFault("Missing endpointreference");
        }

        //TODO: This is not particularly pretty, make WebService have a W3Cendpointreference variable instead of String?
        String endpointReference = null;
        try {
            endpointReference = ServiceUtilities.getAddress(consumerEndpoint);
        } catch (IllegalAccessException e) {
            ServiceUtilities.throwSubscribeCreationFailedFault("EndpointReference malformated or missing.");
        }

        FilterType filters = subscribeRequest.getFilter();

        Map<QName, Object> filtersPresent = null;

        if (filters != null) {
            filtersPresent = new HashMap<>();

            for (Object o : filters.getAny()) {

                if (o instanceof JAXBElement) {
                    JAXBElement filter = (JAXBElement) o;

                    // Filter legality checks
                    if (filterSupport.supportsFilter(filter.getName(), filter.getValue(), namespaceContext)) {
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
                System.out.println(subscribeRequest.getInitialTerminationTime().getValue());
                terminationTime = ServiceUtilities.interpretTerminationTime(subscribeRequest.getInitialTerminationTime().getValue());

                if (terminationTime < System.currentTimeMillis()) {
                    ServiceUtilities.throwUnacceptableInitialTerminationTimeFault("Termination time can not be before 'now'");
                }

            } catch (UnacceptableTerminationTimeFault unacceptableTerminationTimeFault) {
                ServiceUtilities.throwUnacceptableInitialTerminationTimeFault("Malformated termination time");
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
            ServiceUtilities.throwUnacceptableInitialTerminationTimeFault("Internal error: The date was not convertable to a gregorian calendar-instance. If the problem persists," +
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

        // TODO
        // TODO


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
