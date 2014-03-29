package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import junit.framework.TestCase;
import org.ntnunotif.wsnu.base.internal.SoapUnpackingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.WebServiceConnector;
import org.ntnunotif.wsnu.base.util.Information;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.activation.UnsupportedDataTypeException;
import javax.jws.WebParam;
import java.util.List;

/**
 * Created by tormod on 24.03.14.
 */
public class AbstractNotificationProducerTest extends TestCase {
    AbstractNotificationProducer producer;
    SoapUnpackingHub hub;


    public void setUp() throws Exception {
        super.setUp();
        hub = new SoapUnpackingHub();

        producer = new AbstractNotificationProducer(hub) {
            @Override
            public boolean keyExists(String key) {
                return false;
            }

            @Override
            public List<String> getRecipients(Notify notify) {
                return null;
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
            public Object acceptSoapMessage(@WebParam Envelope envelope, @Information RequestInformation requestInformation) {
                return null;
            }

            @Override
            public InternalMessage acceptRequest(@Information RequestInformation requestInformation) {
                return null;
            }

            @Override
            public Hub quickBuild() {
                return null;
            }

            @Override
            public Hub quickBuild(Class<? extends WebServiceConnector> connectorClass, Object... args) throws UnsupportedDataTypeException {
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
