package integration;

import junit.framework.TestCase;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import java.util.ArrayList;

/**
 * Created by tormod on 3/13/14.
 */
public class UnpackingConnectorTest extends TestCase{

    private UnpackingConnector consumerConnector;
    private NotificationConsumer consumer;
    private ArrayList<Object> messages;

    private boolean stackTester = false;

    public void setUp() throws Exception {
        super.setUp();
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
        assertTrue((message.statusCode & InternalMessage.STATUS_HAS_MESSAGE) == 0);
        assertTrue(stackTester);
    }

    @Test
    public void testGetServiceType(){
        Class someClass = consumerConnector.getServiceType();
        assertEquals(someClass, consumer.getClass());
    }
}
