package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import com.sun.xml.internal.stream.buffer.XMLStreamBufferResult;
import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.UnpackingRequestInformationConnector;
import org.ntnunotif.wsnu.base.internal.WebServiceConnector;
import org.ntnunotif.wsnu.base.util.EndpointParam;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.bf_2.BaseFaultType;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;
import org.w3._2001._12.soap_envelope.Header;

import javax.activation.UnsupportedDataTypeException;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.util.JAXBResult;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Result;
import javax.xml.ws.Service;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    private HashMap<String, String> _subscriptions;
    private HashMap<String, Long> _terminationTimes;

    /**
     * Constructor taking a hub as the reference
     * @param hub
     */
    public SimpleNotificationProducer(Hub hub) {
        super(hub);
        _subscriptions = new HashMap<>();
        _terminationTimes = new HashMap<>();
    }

    public SimpleNotificationProducer(){
        super();
        _subscriptions = new HashMap<>();
        _terminationTimes = new HashMap<>();
    }


    @Override
    public boolean keyExists(String key) {
        return _subscriptions.containsKey(key);
    }

    @Override
    public List<String> getRecipients(Notify notify) {
        return new ArrayList(_subscriptions.values());
    }

    @Override
    @WebMethod(operationName = "Subscribe")
    public SubscribeResponse subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe",
                                                 targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Subscribe
                                                 subscribeRequest, RequestInformation requestInformation) throws
                                                 NotifyMessageNotSupportedFault, UnrecognizedPolicyRequestFault,
                                                 TopicExpressionDialectUnknownFault, ResourceUnknownFault,
                                                 InvalidTopicExpressionFault, UnsupportedPolicyRequestFault,
                                                 InvalidFilterFault, InvalidProducerPropertiesExpressionFault, UnacceptableInitialTerminationTimeFault,
                                                 SubscribeCreationFailedFault, TopicNotSupportedFault, InvalidMessageContentExpressionFault {
            Log.d("SimpleNotificationProducer", "Got new subscription request");
            Result result = new XMLStreamBufferResult();
            subscribeRequest.getConsumerReference().writeTo(result);

            System.out.println(result.toString());

            String consumerEndpoint = subscribeRequest.getConsumerReference().toString();
            System.out.println(subscribeRequest.getConsumerReference().toString());

            if(consumerEndpoint == null){
                throw new SubscribeCreationFailedFault("Missing EndpointReference");
            }

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
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            response.setTerminationTime(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.d("SimpleNotificationProducer", "Subscription request generated UnacceptableIntialTerminationTimeFault");
            throw new UnacceptableInitialTerminationTimeFault();
        }

        String newSubscriptionKey = generateSubscriptionKey();
        String subscriptionEndpoint = generateSubscriptionURL(newSubscriptionKey);

        /* Set up the subscription */
        _subscriptions.put(subscriptionEndpoint, consumerEndpoint);
        Log.d("SimpleNotificationProducer", "Added new subscription");

        return response;
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
}
