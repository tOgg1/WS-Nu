package org.ntnunotif.wsnu.base.net;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;

/**
 * Created by tormod on 06.06.14.
 */
public class ApplicationServerConfigTest {

    private static ApplicationServer server;

    @BeforeClass
    public static void setUpClass(){
        ApplicationServer.useConfigFile = false;
        try {
            server = ApplicationServer.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception{
        ApplicationServer.useConfigFile = true;
        ApplicationServer.getInstance().stop();
    }

    @Test
    public void testNonDefaultConnector() throws Exception {

        server.addStandardConnector("127.0.0.1", 8081);

        SoapForwardingHub hub = new SoapForwardingHub();
        server.start(hub);
    }

    @Test
    public void testNonDefaultConfigFile() throws Exception {

    }
}
