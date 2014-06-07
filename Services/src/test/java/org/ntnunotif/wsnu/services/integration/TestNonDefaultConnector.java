package org.ntnunotif.wsnu.services.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;

/**
 * Created by tormod on 06.06.14.
 */
public class TestNonDefaultConnector {
    private static NotificationConsumer consumer;
    private static ApplicationServer server;


    @BeforeClass
    public static void setUpClass() throws Exception {
        consumer = new NotificationConsumer();

        ApplicationServer.useConfigFile = false;
        server = ApplicationServer.getInstance();
        server.addStandardConnector("127.0.0.1", 8081);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        server.stop();
        ApplicationServer.useConfigFile = true;
    }

    @Test
    public void testQuickBuild() throws Exception {
        consumer.quickBuild("myConsumer");
    }
}
