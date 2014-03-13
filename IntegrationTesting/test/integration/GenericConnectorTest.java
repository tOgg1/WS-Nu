package integration;

import com.sun.swing.internal.plaf.synth.resources.synth_sv;
import junit.framework.TestCase;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.GenericConnector;
import org.ntnunotif.wsnu.base.internal.InternalMessage;
import org.ntnunotif.wsnu.services.NotificationConsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.oasis_open.docs.wsn.b_2.Notify;

import javax.jws.WebMethod;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tormod on 3/13/14.
 */
public class GenericConnectorTest extends TestCase{

    private GenericConnector consumerConnector;
    private NotificationConsumer consumer;
    private ArrayList<Object> messages;

    private boolean stackTester = false;

    public void setUp() throws Exception {
        super.setUp();
        consumer = new NotificationConsumer();
        consumerConnector = new GenericConnector(consumer);
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
        InternalMessage message = consumerConnector.acceptMessage(messages.get(0));
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
        assertTrue(functionality.values().contains(notify));

    }
}
