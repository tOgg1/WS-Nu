package org.ntnunotif.wsnu.services.general;

import org.junit.Before;
import org.junit.Test;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;

/**
 * Created by tormod on 30.03.14.
 */
public class WebServiceTest {
    private static NotificationConsumer consumer;

    @Before
    public void setUp() throws Exception {
        consumer = new NotificationConsumer();
        consumer.forceEndpointReference("myWebService");
    }

    @Test
    public void testGenerateWSDLandXSDSchemas() throws Exception {
        consumer.generateWSDLandXSDSchemas();
    }
}
