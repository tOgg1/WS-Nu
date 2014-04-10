package org.ntnunotif.wsnu.services.implementations.notificationbroker;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
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
import javax.xml.namespace.NamespaceContext;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by tormod on 06.04.14.
 */
public class GenericNotificationBroker extends AbstractNotificationBroker {

    private HashMap<String, ServiceUtilities.EndpointTerminationTuple> _subscriptions;
    private HashMap<String, ServiceUtilities.EndpointTerminationTuple> _publishers;

    public GenericNotificationBroker() {
        super();
    }

    @Override
    public boolean keyExists(String key) {
        return false;
    }

    @Override
    protected Collection<String> getAllRecipients() {
        return null;
    }

    @Override
    protected Notify getRecipientFilteredNotify(String recipient, Notify notify, NamespaceContext namespaceContext) {
        return null;
    }

    @Override
    @Oneway
    @WebMethod(operationName = "Notify")
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                       Notify notify) {
        _eventSupport.fireNotificationEvent(notify, _connection.getReqeustInformation());
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

        String endpointReference = null;
        try {
            endpointReference = ServiceUtilities.getAddress(registerPublisherRequest.getPublisherReference());
        } catch (IllegalAccessException e) {
            ServiceUtilities.throwPublisherRegistrationFailedFault("en", "Could not register publisher, failed to " +
                    "understand the endpoint reference");
        }

        long terminationTime = registerPublisherRequest.getInitialTerminationTime().toGregorianCalendar().getTimeInMillis();

        if(terminationTime < System.currentTimeMillis()){
            throw new UnacceptableInitialTerminationTimeFault("Invalid termination time. Can't be before current time");
        }

        String newSubscriptionKey = generateSubscriptionKey();
        String subscriptionEndpoint = generateSubscriptionURL(newSubscriptionKey);

        _publishers.put(newSubscriptionKey, new ServiceUtilities.EndpointTerminationTuple(endpointReference, terminationTime));

        RegisterPublisherResponse response = new RegisterPublisherResponse();

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(getEndpointReference() +""+ subscriptionEndpoint);

        response.setConsumerReference(builder.build());
        //TODO: response.setPublisherRegistrationReference();
        return response;
    }

    @Override
    public SubscribeResponse subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Subscribe subscribeRequest) throws NotifyMessageNotSupportedFault, UnrecognizedPolicyRequestFault, TopicExpressionDialectUnknownFault, ResourceUnknownFault, InvalidTopicExpressionFault, UnsupportedPolicyRequestFault, InvalidFilterFault, InvalidProducerPropertiesExpressionFault, UnacceptableInitialTerminationTimeFault, SubscribeCreationFailedFault, TopicNotSupportedFault, InvalidMessageContentExpressionFault {
        return null;
    }

    @Override
    public GetCurrentMessageResponse getCurrentMessage(@WebParam(partName = "GetCurrentMessageRequest", name = "GetCurrentMessage", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") GetCurrentMessage getCurrentMessageRequest) throws InvalidTopicExpressionFault, TopicExpressionDialectUnknownFault, MultipleTopicsSpecifiedFault, ResourceUnknownFault, NoCurrentMessageOnTopicFault, TopicNotSupportedFault {
        return null;
    }

    @Override
    public SoapForwardingHub quickBuild(String endpointReference) {
        return null;
    }
}
