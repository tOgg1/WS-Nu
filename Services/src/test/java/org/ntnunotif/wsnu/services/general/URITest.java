package org.ntnunotif.wsnu.services.general;

import org.junit.Before;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;

/**
 * Created by tormod on 07.04.14.
 */
public class URITest {
    NotificationConsumer consumer;

    @Before
    public void setUp(){
        consumer = new NotificationConsumer();
    }

    /* Fails on MAVEN build in windows; cannot find wsgen
    @Test
    public void testConsumer() throws Exception {
        Hub hub = consumer.quickBuild("http://localhost:8080/myWebService");
        consumer.generateWSDLandXSDSchemas();
        Thread.sleep(30000);

    }
    */
}
