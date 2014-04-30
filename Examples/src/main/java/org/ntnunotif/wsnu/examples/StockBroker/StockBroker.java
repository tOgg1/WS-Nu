package org.ntnunotif.wsnu.examples.StockBroker;

import org.ntnunotif.wsnu.services.implementations.notificationbroker.GenericNotificationBroker;
import org.ntnunotif.wsnu.services.implementations.publisherregistrationmanager.SimplePublisherRegistrationManager;

/**
 * Created by tormod on 29.04.14.
 */
public class StockBroker {

    private GenericNotificationBroker broker;
    private SimplePublisherRegistrationManager manager;

    public StockBroker() {
    }

    public void start(){
        broker = new GenericNotificationBroker();
        broker.setCacheMessages(true);
        broker.setDemandRegistered(true);
        broker.quickBuild("myBroker");

        manager = new SimplePublisherRegistrationManager();

        broker.setRegistrationManager(manager);
    }

    public static void main(String[] args) {
        StockBroker broker = new StockBroker();
        broker.start();
    }
}
