package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.util.EndpointParam;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.bw_2.*;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;

/**
 * Created by tormod on 23.03.14.
 */
public interface NotificationProducer {

    @WebResult(name = "SubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "SubscribeResponse")
    @WebMethod(operationName = "Subscribe")
    public org.oasis_open.docs.wsn.b_2.SubscribeResponse subscribe(
            @WebParam(partName = "SubscribeRequest", name = "Subscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
            org.oasis_open.docs.wsn.b_2.Subscribe subscribeRequest, RequestInformation requestInformation
    ) throws NotifyMessageNotSupportedFault, UnrecognizedPolicyRequestFault, TopicExpressionDialectUnknownFault, org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, InvalidTopicExpressionFault, UnsupportedPolicyRequestFault, InvalidFilterFault, InvalidProducerPropertiesExpressionFault, UnacceptableInitialTerminationTimeFault, SubscribeCreationFailedFault, TopicNotSupportedFault, InvalidMessageContentExpressionFault;

    @WebResult(name = "GetCurrentMessageResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "GetCurrentMessageResponse")
    @WebMethod(operationName = "GetCurrentMessage")
    public org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse getCurrentMessage(
            @WebParam(partName = "GetCurrentMessageRequest", name = "GetCurrentMessage", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
            org.oasis_open.docs.wsn.b_2.GetCurrentMessage getCurrentMessageRequest
    ) throws InvalidTopicExpressionFault, TopicExpressionDialectUnknownFault, MultipleTopicsSpecifiedFault, org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, NoCurrentMessageOnTopicFault, TopicNotSupportedFault;
}
