package base;

import org.junit.Before;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.SimpleNotificationProducer;

import javax.jws.WebService;

/**
 * Created by tormod on 25.03.14.
 */
public class WebServiceConnectorTest {
    private SimpleNotificationProducer producer;
    private NotificationConsumer consumer;
    private UnpackingConnector one, two, three;

    @WebService
    private class WebServiceWithoutEndPoint{

    }

    @Before
    public void setUp() throws Exception {
        producer = new SimpleNotificationProducer();
        consumer = new NotificationConsumer();
    }

    @Test
    public void testConstructor() throws Exception {
        one = new UnpackingConnector(producer);
        two = new UnpackingConnector(consumer);
        three = new UnpackingConnector(new WebServiceWithoutEndPoint());
    }
}
