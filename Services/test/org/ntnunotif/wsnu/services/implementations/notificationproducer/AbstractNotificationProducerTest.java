package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import junit.framework.TestCase;
import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.Information;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.jws.WebParam;

/**
 * Created by tormod on 24.03.14.
 */
public class AbstractNotificationProducerTest extends TestCase {
    AbstractNotificationProducer producer;
    ForwardingHub hub;


    public void setUp() throws Exception {
        super.setUp();
        hub = new ForwardingHub();

        producer = new AbstractNotificationProducer(hub) {
            @Override
            public boolean keyExists(String key) {
                return false;
            }

            @Override
            public SubscribeResponse subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Subscribe subscribeRequest, @Information RequestInformation requestInformation) throws NotifyMessageNotSupportedFault, UnrecognizedPolicyRequestFault, TopicExpressionDialectUnknownFault, ResourceUnknownFault, InvalidTopicExpressionFault, UnsupportedPolicyRequestFault, InvalidFilterFault, InvalidProducerPropertiesExpressionFault, UnacceptableInitialTerminationTimeFault, SubscribeCreationFailedFault, TopicNotSupportedFault, InvalidMessageContentExpressionFault {
                return null;
            }

            @Override
            public GetCurrentMessageResponse getCurrentMessage(@WebParam(partName = "GetCurrentMessageRequest", name = "GetCurrentMessage", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") GetCurrentMessage getCurrentMessageRequest) throws InvalidTopicExpressionFault, TopicExpressionDialectUnknownFault, MultipleTopicsSpecifiedFault, ResourceUnknownFault, NoCurrentMessageOnTopicFault, TopicNotSupportedFault {
                return null;
            }

            @Override
            public void acceptSoapMessage(@WebParam Envelope envelope) {

            }

            @Override
            public Hub quickBuild() {
                return null;
            }
        };
    }

    public void tearDown() throws Exception {

    }

    public void testGenerateSubscriptionKey() throws Exception {
        String newKey = producer.generateNewSubscriptionURL();
        System.out.println(newKey);
        assertTrue(newKey != null);
    }
}
