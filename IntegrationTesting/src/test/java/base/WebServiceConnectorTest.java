package base;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.GenericNotificationProducer;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.SimpleNotificationProducer;
import org.ntnunotif.wsnu.services.implementations.subscriptionmanager.SimplePausableSubscriptionManager;

import javax.jws.WebService;

/**
 * Created by tormod on 25.03.14.
 */
public class WebServiceConnectorTest {
    private static SimpleNotificationProducer producer;
    private static NotificationConsumer consumer;
    private static UnpackingConnector one, two, three;

    @BeforeClass
    public static void setUpClass(){
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);
        producer = new SimpleNotificationProducer();
        consumer = new NotificationConsumer();
    }

    @WebService
    private class WebServiceWithoutEndPoint {

    }

    @Test
    public void testConstructor() throws Exception {
        one = new UnpackingConnector(producer);
        two = new UnpackingConnector(consumer);
        three = new UnpackingConnector(new WebServiceWithoutEndPoint());
    }

    @Test
    public void testQuickBuild() throws Exception {
        GenericNotificationProducer producer  = new GenericNotificationProducer();
        NotificationConsumer consumer = new NotificationConsumer();
        SimplePausableSubscriptionManager pausableManager = new SimplePausableSubscriptionManager();

        Hub myHub = producer.quickBuild("myProducer");
        consumer.quickBuild("myConsumer", myHub);
        pausableManager.quickBuild("myManager", myHub);
    }
}
