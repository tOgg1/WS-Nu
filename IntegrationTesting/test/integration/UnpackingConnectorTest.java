package integration;

import junit.framework.TestCase;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.DefaultHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.notificationconsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

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
        consumer = new NotificationConsumer(new DefaultHub());
        consumerConnector = new UnpackingConnector(consumer);
        messages = new ArrayList<Object>();
        messages.add(new Notify());
    }

    @Test
    public void testAcceptMessageWithNotificationConsumer(){
        ConsumerListener listen = new ConsumerListener() {
            @Override
            public void notify(NotificationEvent event) {
                System.out.println(event.getRaw());
                assertTrue(messages.contains(event.getRaw()));
                stackTester = true;
            }
        };
        consumer.addConsumerListener(listen);
        stackTester = false;
        InternalMessage sendMessage = new InternalMessage(InternalMessage.STATUS_OK, messages.get(0));
        InternalMessage message = consumerConnector.acceptMessage(sendMessage);
        assertTrue((message.statusCode & InternalMessage.STATUS_HAS_RETURNING_MESSAGE) == 0);
        assertTrue(stackTester);
    }

    @Test
    public void testGetServiceType(){
        Class someClass = consumerConnector.getServiceType();
        assertEquals(someClass, consumer.getClass());
    }

    @Test
    public void testGetServiceFunctionalityForConsumer() throws NoSuchMethodException {
        Method[] methods = org.oasis_open.docs.wsn.bw_2.NotificationConsumer.class.getMethods();
        HashMap<String, Method> functionality = consumerConnector.getServiceFunctionality();
        Method notify = methods[0];

        assertTrue(functionality.size() == 1);
        Method notifyFromFunc = functionality.get("Notify");
        assertTrue(notify.getName() == notifyFromFunc.getName());
    }
}
