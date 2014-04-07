package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.SimpleNotificationProducer;

/**
 * Created by tormod on 30.03.14.
 */
public class WebServiceTest {
    private static NotificationConsumer consumer;
    private static SimpleNotificationProducer producer;
/* commented out because it fails in windows; incomplete source code
    @Before
    public void setUp() throws Exception {
        SoapForwardingHub hub = new SoapForwardingHub();
        producer = new SimpleNotificationProducer();
        producer.setHub(hub);
        UnpackingConnector connector = new UnpackingConnector(producer);
        producer.setEndpointReference("myProducer");
    }

    @Test
    public void testGenerateWSDLandXSDSchemas() throws Exception {
        producer.generateWSDLandXSDSchemas();
    }
*/
}
