package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.UnpackingRequestInformationConnector;
import org.ntnunotif.wsnu.base.internal.WebServiceConnector;
import org.ntnunotif.wsnu.base.util.EndpointParam;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;
import org.w3._2001._12.soap_envelope.Header;

import javax.activation.UnsupportedDataTypeException;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.lang.reflect.Constructor;
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

    private HashMap<String, String> _subscriptions;

    /**
     * Constructor taking a hub as the reference
     * @param hub
     */
    public SimpleNotificationProducer(Hub hub) {
        super(hub);
    }

    public SimpleNotificationProducer(){
        super();
    }


    @Override
    public boolean keyExists(String key) {
        return false;
    }


    @Override
    public SubscribeResponse subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe",
                                                 targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Subscribe
                                                 subscribeRequest, RequestInformation requestInformation) throws
                                                 NotifyMessageNotSupportedFault, UnrecognizedPolicyRequestFault,
                                                 TopicExpressionDialectUnknownFault, ResourceUnknownFault,
                                                 InvalidTopicExpressionFault, UnsupportedPolicyRequestFault,
                                                 InvalidFilterFault, InvalidProducerPropertiesExpressionFault, UnacceptableInitialTerminationTimeFault,
                                                 SubscribeCreationFailedFault, TopicNotSupportedFault, InvalidMessageContentExpressionFault {
        String consumerEndpoint = subscribeRequest.getConsumerReference().toString();
        FilterType filter = subscribeRequest.getFilter();
        return null;
    }

    @Override
    @WebResult(name = "GetCurrentMessageResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "GetCurrentMessageResponse")
    @WebMethod(operationName = "GetCurrentMessage")
    public GetCurrentMessageResponse getCurrentMessage(@WebParam(partName = "GetCurrentMessageRequest", name = "GetCurrentMessage",
                                                                 targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                                                       GetCurrentMessage getCurrentMessageRequest)
                                                       throws InvalidTopicExpressionFault, TopicExpressionDialectUnknownFault,
                                                       MultipleTopicsSpecifiedFault, ResourceUnknownFault, NoCurrentMessageOnTopicFault,
                                                       TopicNotSupportedFault {
        return null;
    }

    @Override
    @WebMethod(operationName = "acceptSoapMessage")
    public synchronized Object acceptSoapMessage(Envelope envelope) {
        Header header = envelope.getHeader();
        Body body = envelope.getBody();

        List<Object> headercontent = header.getAny();
        List<Object> bodyContent = body.getAny();

        //TODO: Handle bodyContent

        return null;
    }

    @Override
    public Hub quickBuild() {
        try {
            ForwardingHub hub = new ForwardingHub();

            /* This is the most reasonable connector for this NotificationProducer */
            UnpackingRequestInformationConnector connector = new UnpackingRequestInformationConnector(this);
            hub.registerService(connector);
            this._hub = hub;
            return hub;

        } catch (Exception e) {
            throw new RuntimeException("Unable to quickbuild: " + e.getMessage());
        }
    }

    @Override
    public Hub quickBuild(Class<? extends WebServiceConnector> connectorClass, Object... args) throws UnsupportedDataTypeException {
        try {
            ForwardingHub hub = new ForwardingHub();

            Constructor constructor = connectorClass.getConstructor();
            WebServiceConnector connector = (WebServiceConnector)constructor.newInstance(this, args);
            hub.registerService(connector);
            this._hub = hub;
            return hub;
        }catch (Exception e){
            throw new RuntimeException("Unable to quickbuild: " + e.getMessage());
        }
    }
}
