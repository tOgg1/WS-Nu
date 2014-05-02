package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.junit.BeforeClass;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionEvent;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebParam;
import java.util.Collection;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by tormod on 24.03.14.
 */
public class AbstractNotificationProducerTest {
    private static AbstractNotificationProducer producer;
    private static SoapForwardingHub hub;

    @BeforeClass
    public void setUp() throws Exception {
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        hub = new SoapForwardingHub();
        producer = new AbstractNotificationProducer() {
            @Override
            public boolean keyExists(String key) {
                return false;
            }

            @Override
            protected Collection<String> getAllRecipients() {
                return null;
            }

            @Override
            protected Notify getRecipientFilteredNotify(String recipient, Notify notify, NuNamespaceContextResolver namespaceContext) {
                return null;
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
            protected String getEndpointReferenceOfRecipient(String subscriptionKey) {
                return null;
            }

            @Override
            public void subscriptionChanged(SubscriptionEvent event) {

            }

            @Override
            public SoapForwardingHub quickBuild(String endpointReference) {
                return null;
            }
        };
    }

    public void tearDown() throws Exception {
        ApplicationServer.getInstance().stop();
        hub.stop();
    }

    public void testGenerateSubscriptionKey() throws Exception {
        String newKey = producer.generateNewHashedURL("subscription");
        assertTrue("Newkey was null", newKey != null);
    }
}
