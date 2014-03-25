import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.MappingConnector;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.SimpleNotificationProducer;

import javax.activation.UnsupportedDataTypeException;
import java.util.HashMap;

/**
 * Created by tormod on 25.03.14.
 * @author Tormod Haugland
 */
public class SimpleProducerWithMappingConnector {

    private SimpleNotificationProducer producer;
    private Hub hub;

    public SimpleProducerWithMappingConnector() {

        producer = new SimpleNotificationProducer();

        /* Establish method relations (see SimpleNotificationProducer) */
        HashMap<String, String> methodMaps = new HashMap<>();

        methodMaps.put("GetCurrentMessage", "getCurrentMessage");
        methodMaps.put("Subscribe", "subscribe");
        methodMaps.put("Envelope", "acceptSoapMessage");

        try {
            /* Adds a MappingConnector by passing in its class and its (extra arguments) in quickBuild */
            hub = producer.quickBuild(MappingConnector.class, methodMaps);
            System.out.println("WORKS");
        } catch (UnsupportedDataTypeException e) {
            e.printStackTrace();
        }
        System.out.println(hub);
    }

    public static void main(String[] args) {
        SimpleProducerWithMappingConnector prod = new SimpleProducerWithMappingConnector();
    }
}
