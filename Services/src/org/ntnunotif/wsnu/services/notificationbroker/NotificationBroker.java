package org.ntnunotif.wsnu.services.notificationbroker;

import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationFailedFault;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationRejectedFault;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebParam;

/**
 * Created by tormod on 3/11/14.
 */
public class NotificationBroker implements org.oasis_open.docs.wsn.brw_2.NotificationBroker {
    @Override
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Notify notify) {
        
    }

    @Override
    public RegisterPublisherResponse registerPublisher(@WebParam(partName = "RegisterPublisherRequest", name = "RegisterPublisher", targetNamespace = "http://docs.oasis-open.org/wsn/br-2") RegisterPublisher registerPublisherRequest) throws InvalidTopicExpressionFault, PublisherRegistrationFailedFault, ResourceUnknownFault, PublisherRegistrationRejectedFault, UnacceptableInitialTerminationTimeFault, TopicNotSupportedFault {
        return null;
    }

    @Override
    public GetCurrentMessageResponse getCurrentMessage(@WebParam(partName = "GetCurrentMessageRequest", name = "GetCurrentMessage", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") GetCurrentMessage getCurrentMessageRequest) throws InvalidTopicExpressionFault, NoCurrentMessageOnTopicFault, TopicExpressionDialectUnknownFault, ResourceUnknownFault, MultipleTopicsSpecifiedFault, TopicNotSupportedFault {
        return null;
    }

    @Override
    public SubscribeResponse subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Subscribe subscribeRequest) throws InvalidTopicExpressionFault, InvalidProducerPropertiesExpressionFault, TopicExpressionDialectUnknownFault, UnsupportedPolicyRequestFault, InvalidFilterFault, ResourceUnknownFault, NotifyMessageNotSupportedFault, UnrecognizedPolicyRequestFault, SubscribeCreationFailedFault, UnacceptableInitialTerminationTimeFault, InvalidMessageContentExpressionFault, TopicNotSupportedFault {
        return null;
    }
}
