package org.ntnunotif.wsnu.services.implementations.notificationbroker;

import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationFailedFault;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationRejectedFault;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Created by tormod on 3/11/14.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/brw-2", name = "NotificationBroker")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class NotificationBroker implements org.ntnunotif.wsnu.services.general.NotificationBroker{

    @Override
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Notify notify) {

    }

    @Override
    public RegisterPublisherResponse registerPublisher(@WebParam(partName = "RegisterPublisherRequest", name = "RegisterPublisher",
                                                                 targetNamespace = "http://docs.oasis-open.org/wsn/br-2")
                                                       RegisterPublisher registerPublisherRequest, RequestInformation requestInformation)
                                                       throws InvalidTopicExpressionFault, PublisherRegistrationFailedFault,
                                                              ResourceUnknownFault, PublisherRegistrationRejectedFault,
                                                              UnacceptableInitialTerminationTimeFault, TopicNotSupportedFault {
        return null;
    }

    @Override
    public GetCurrentMessageResponse getCurrentMessage(@WebParam(partName = "GetCurrentMessageRequest", name = "GetCurrentMessage",
                                                                 targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                                                       GetCurrentMessage getCurrentMessageRequest)
                                                       throws InvalidTopicExpressionFault, NoCurrentMessageOnTopicFault,
                                                       TopicExpressionDialectUnknownFault, ResourceUnknownFault,
                                                       MultipleTopicsSpecifiedFault, TopicNotSupportedFault {
        return null;
    }

    @Override
    public SubscribeResponse subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Subscribe subscribeRequest, RequestInformation requestInformation) throws InvalidTopicExpressionFault, InvalidProducerPropertiesExpressionFault, TopicExpressionDialectUnknownFault, UnsupportedPolicyRequestFault, InvalidFilterFault, ResourceUnknownFault, NotifyMessageNotSupportedFault, UnrecognizedPolicyRequestFault, SubscribeCreationFailedFault, UnacceptableInitialTerminationTimeFault, InvalidMessageContentExpressionFault, TopicNotSupportedFault {
        return null;
    }
}
