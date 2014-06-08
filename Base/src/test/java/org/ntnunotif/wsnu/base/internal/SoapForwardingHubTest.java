package org.ntnunotif.wsnu.base.internal;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.util.Connection;
import org.ntnunotif.wsnu.base.util.EndpointReference;
import org.ntnunotif.wsnu.base.util.Log;

import javax.jws.WebService;
import java.util.Collection;

import static junit.framework.TestCase.*;

/**
 * Created by tormod on 08.06.14.
 */
public class SoapForwardingHubTest {

    private static SoapForwardingHub hub;

    private static Object webServiceOne;
    private static Object webServiceTwo;
    private static Object webServiceThree;
    private static UnpackingConnector connectorOne;
    private static UnpackingConnector connectorTwo;
    private static UnpackingConnector connectorThree;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Log.setEnableWarnings(false);
        hub = new SoapForwardingHub();

        webServiceOne = new SimpleWebService();
        webServiceTwo = new SimpleWebService();
        webServiceThree = new SimpleWebService();

        connectorOne = new UnpackingConnector(webServiceOne);
        connectorTwo = new UnpackingConnector(webServiceTwo);
        connectorThree = new UnpackingConnector(webServiceThree);

    }

    @After
    public void tearDown() throws Exception {
        hub.clearAllServices();
    }

    @Test
    public void testRegisterService() throws Exception {

        hub.registerService(connectorOne);
        hub.registerService(connectorTwo);
        hub.registerService(connectorThree);

        Collection<ServiceConnection> connections = hub.getServices();

        assertTrue(connections.contains(connectorOne));
        assertTrue(connections.contains(connectorTwo));
        assertTrue(connections.contains(connectorThree));
        assertEquals(3, connections.size());
    }


    @Test
    public void testRegisterDuplicates() throws Exception {
        hub.registerService(connectorOne);
        hub.registerService(connectorOne);
        hub.registerService(connectorOne);
        hub.registerService(connectorTwo);

        Collection<ServiceConnection> connections = hub.getServices();

        assertTrue(connections.contains(connectorOne));
        assertTrue(connections.contains(connectorTwo));
        assertEquals(2, connections.size());
    }

    @Test
    public void testRemoveService() throws Exception {
        hub.registerService(connectorOne);
        hub.registerService(connectorTwo);

        Collection<ServiceConnection> connections = hub.getServices();
        assertEquals(2, connections.size());

        hub.removeService(connectorOne);

        assertEquals(1, connections.size());
        assertFalse(connections.contains(connectorOne));
        assertTrue(connections.contains(connectorTwo));

        hub.removeService(connectorTwo);

        assertEquals(0, connections.size());
        assertFalse(connections.contains(connectorTwo));

    }

    @Test
    public void testRemoveServiceByObject() throws Exception {
        hub.registerService(connectorOne);

        Collection<ServiceConnection> connections = hub.getServices();
        assertEquals(1, connections.size());

        hub.removeService(webServiceOne);

        assertEquals(0, connections.size());
    }

    @WebService
    public static class SimpleWebService{
        @EndpointReference
        public String endpointReference;

        @Connection
        public Object connection;
    }


}
