package integration;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by tormod on 3/13/14.
 */
public class UnpackingConnectorTest {

    private static UnpackingConnector consumerConnector;
    private static NotificationConsumer consumer;
    private static ArrayList<Object> messages;

    private boolean stackTester = false;

    @BeforeClass
    public static void setUp() throws Exception {
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        consumer = new NotificationConsumer(new SoapForwardingHub());
        consumerConnector = new UnpackingConnector(consumer);
        messages = new ArrayList<Object>();
        Envelope env = new Envelope();
        Body body = new Body();
        body.getAny().add(new Notify());
        env.setBody(body);
        messages.add(env);
    }

    @Test
    public void testAcceptMessageWithNotificationConsumer(){
        ConsumerListener listen = new ConsumerListener() {
            @Override
            public void notify(NotificationEvent event) {
                assertTrue(event.getRaw() != null);
                stackTester = true;
            }
        };
        consumer.addConsumerListener(listen);
        stackTester = false;
        InternalMessage sendMessage = new InternalMessage(InternalMessage.STATUS_OK, messages.get(0));
        InternalMessage message = consumerConnector.acceptMessage(sendMessage);
        assertTrue("Message from connector did not have returnMessage", (message.statusCode & InternalMessage.STATUS_HAS_MESSAGE) == 0);
        assertTrue("The stacktest-flag failed, notify never got called", stackTester);
    }

    @Test
    public void testGetServiceType(){
        Class someClass = consumerConnector.getServiceType();
        assertEquals("Class of connector is wrong when returned from connector", someClass, consumer.getClass());
    }
}
