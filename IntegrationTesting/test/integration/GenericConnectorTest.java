package integration;

import junit.framework.TestCase;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.GenericConnector;
import org.ntnunotif.wsnu.services.NotificationConsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.util.ArrayList;

/**
 * Created by tormod on 3/13/14.
 */
public class GenericConnectorTest extends TestCase{

    private GenericConnector connector;
    private NotificationConsumer consumer;
    private ArrayList<Object> messages;

    private boolean stackTester = false;

    public void setUp() throws Exception {
        super.setUp();
        consumer = new NotificationConsumer();
        connector = new GenericConnector(consumer);
        messages = new ArrayList<Object>();
        messages.add(new Notify());
    }

    @Test
    public void testAcceptMessage(){
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
        connector.acceptMessage(messages.get(0));
        assertTrue(stackTester);
    }

    @Test
    public void testGetServiceType(){
        Class someClass = connector.getServiceType();
        assertEquals(someClass, consumer.getClass());

    }

    @Test
    public void testGetServiceFunctionality(){

    }
}
