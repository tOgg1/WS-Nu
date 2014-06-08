package org.ntnunotif.wsnu.base.net;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
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

    }

    @Test
    public void testNonDefaultConfigFile() throws Exception {

    }
}
