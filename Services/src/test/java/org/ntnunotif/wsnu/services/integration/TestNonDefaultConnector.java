package org.ntnunotif.wsnu.services.integration;

import org.eclipse.jetty.server.Connector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumerImpl;

import static junit.framework.TestCase.assertTrue;

/**
 *
 */
public class TestNonDefaultConnector {
    private static NotificationConsumerImpl consumer;
    private static ApplicationServer server;


    @BeforeClass
    public static void setUpClass() throws Exception {
        consumer = new NotificationConsumerImpl();

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
        Connector[] connectors = server.getServer().getConnectors();

        assertTrue(connectors.length == 1);

    }
}
