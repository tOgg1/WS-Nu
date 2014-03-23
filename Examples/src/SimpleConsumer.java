import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.internal.DefaultHub;
import org.ntnunotif.wsnu.services.notificationconsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.util.List;

/**
 * Created by tormod on 3/17/14.
 */
public class SimpleConsumer implements ConsumerListener {

    public static void main(String[] args) throws Exception{

        /* Instantiate base-objects, running the server on default ip(localhost) */
        DefaultHub hub = new DefaultHub();

        /* Create Web Service, passing in an EndpointReference*/
        NotificationConsumer consumer = new NotificationConsumer(hub, "http://tormodhaugland.com");

        /* The connector between the hub/applicatonserver and the Web Service */
        UnpackingConnector connector = new UnpackingConnector(consumer);

        /* Register Web Service with hub, making it eligible to receive messages */
        hub.registerService(connector);

        /* Our implementing class, being a ConsumerListener interface */
        SimpleConsumer simpleConsumer = new SimpleConsumer();
        consumer.addConsumerListener(simpleConsumer);
    }

    public SimpleConsumer() {

    }

    @Override
    public void notify(NotificationEvent event) {
        /* This is a SimpleConsumer, so we just take an event, display its contents, and leave */

        Notify notification = event.getRaw();
        List<Object> everything = notification.getAny();

        for (Object o : everything) {
            System.out.println(o.getClass());
            System.out.println(o);
        }
    }
}
