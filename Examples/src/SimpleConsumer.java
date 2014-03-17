import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.ntnunotif.wsnu.base.internal.GenericConnector;
import org.ntnunotif.wsnu.base.internal.InternalHub;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.services.NotificationConsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Created by tormod on 3/17/14.
 */
public class SimpleConsumer implements ConsumerListener {

    public static void main(String[] args) throws Exception{

        /* Instantiate base-objects, running the server on default ip(localhost) */
        ApplicationServer server = ApplicationServer.getInstance();
        InternalHub hub = new InternalHub();
        server.start(hub);

        /* Create Web Service, passing in an EndpointReference*/
        NotificationConsumer consumer = new NotificationConsumer("http://tormodhaugland.com");

        /* The connector between the hub/applicatonserver and the Web Service */
        GenericConnector connector = new GenericConnector(consumer);

        /* Register Web Service with hub, making it eligible to receive messages */
        hub.registerService(connector);

        /* Our implementing class, being a ConsumerListener interface */
        SimpleConsumer simpleConsumer = new SimpleConsumer();
        consumer.addConsumerListener(simpleConsumer);

        while(true){
            System.out.println("We're running!");
            /* Lets just send it the same notification every 5 seconds */
            InputStream stream = new FileInputStream("Examples/res/server_test_notify.xml");

            HttpClient client = new HttpClient();
            client.start();

            Request request = client.newRequest("http://localhost:8080/");
            request.method(HttpMethod.POST);
            request.content(new InputStreamContentProvider(stream));
            request.send();

            Thread.sleep(5000);
        }
    }

    public SimpleConsumer() {

    }

    @Override
    public void notify(NotificationEvent event) {
        /* This is a simpleconsumer, so we just take an event, display its contents, and leave */

        Notify notification = event.getRaw();
        List<Object> everything = notification.getAny();

        for (Object o : everything) {
            System.out.println(o.getClass());
            System.out.println(o);
        }
    }
}
