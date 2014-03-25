package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.util.EndpointParam;
import org.ntnunotif.wsnu.base.util.EndpointReference;
import org.ntnunotif.wsnu.base.util.Information;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationFailedFault;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationRejectedFault;

import javax.jws.*;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Extension of the default NotificationBroker interface to include _endpointReference methodarguments
 * Created by tormod on 23.03.14.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/brw-2", name = "NotificationBroker")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface NotificationBroker {

    @Oneway
    @WebMethod(operationName = "Notify")
    public void notify(
            @WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
            org.oasis_open.docs.wsn.b_2.Notify notify
    );

    @WebResult(name = "RegisterPublisherResponse", targetNamespace = "http://docs.oasis-open.org/wsn/br-2", partName = "RegisterPublisherResponse")
    @WebMethod(operationName = "RegisterPublisher")
    public org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse registerPublisher(
            @WebParam(partName = "RegisterPublisherRequest", name = "RegisterPublisher", targetNamespace = "http://docs.oasis-open.org/wsn/br-2")
            org.oasis_open.docs.wsn.br_2.RegisterPublisher registerPublisherRequest, @Information RequestInformation requestInformation
    ) throws org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault, PublisherRegistrationFailedFault, org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, PublisherRegistrationRejectedFault, org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault, org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;

    @WebResult(name = "GetCurrentMessageResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "GetCurrentMessageResponse")
    @WebMethod(operationName = "GetCurrentMessage")
    public org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse getCurrentMessage(
            @WebParam(partName = "GetCurrentMessageRequest", name = "GetCurrentMessage", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
            org.oasis_open.docs.wsn.b_2.GetCurrentMessage getCurrentMessageRequest
    ) throws org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault, org.oasis_open.docs.wsn.bw_2.NoCurrentMessageOnTopicFault, org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault, org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault, org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;

    @WebResult(name = "SubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "SubscribeResponse")
    @WebMethod(operationName = "Subscribe")
    public org.oasis_open.docs.wsn.b_2.SubscribeResponse subscribe(
            @WebParam(partName = "SubscribeRequest", name = "Subscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
            org.oasis_open.docs.wsn.b_2.Subscribe subscribeRequest, @Information RequestInformation requestInformation
    ) throws org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault, org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault, org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault, org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault, org.oasis_open.docs.wsn.bw_2.InvalidFilterFault, org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault, org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault, org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault, org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault, org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault, org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;

}