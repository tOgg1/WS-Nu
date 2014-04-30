package org.ntnunotif.wsnu.examples.StockBroker;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.implementations.notificationbroker.GenericNotificationBroker;
import org.ntnunotif.wsnu.services.implementations.publisherregistrationmanager.SimplePublisherRegistrationManager;

/**
 * Created by tormod on 29.04.14.
 */
public class StockBroker {

    private GenericNotificationBroker broker;
    private SimplePublisherRegistrationManager manager;
    private Hub hub;

    public StockBroker() {

    }

    public void start() throws Exception {
        broker = new GenericNotificationBroker();
        broker.setCacheMessages(true);
        broker.setDemandRegistered(true);
        hub = broker.quickBuild("myBroker");
        broker.generateWSDLandXSDSchemas();

        manager = new SimplePublisherRegistrationManager(hub);
        manager.setEndpointReference("registrationManager");
        UnpackingConnector connector = new UnpackingConnector(manager);
        hub.registerService(connector);
        broker.setRegistrationManager(manager);

        ServiceUtilities.InputManager inputManager = new ServiceUtilities.InputManager();
        inputManager.addMethodReroute("exit", "^exit", true, System.class.getDeclaredMethod("exit", Integer.TYPE), this, new ServiceUtilities.Tuple[]{new ServiceUtilities.Tuple(0, 0)});
        inputManager.start();
    }

    public static void main(String[] args) {
        XMLParser.registerReturnObjectPackageWithObjectFactory("org.ntnunotif.wsnu.examples.StockBroker.generated");
        StockBroker broker = new StockBroker();
        try {
            broker.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
