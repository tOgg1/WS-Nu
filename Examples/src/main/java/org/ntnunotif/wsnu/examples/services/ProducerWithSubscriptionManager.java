package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.GenericNotificationProducer;
import org.ntnunotif.wsnu.services.implementations.subscriptionmanager.SimplePausableSubscriptionManager;

/**
 * Created by tormod on 10.05.14.
 */
public class ProducerWithSubscriptionManager {

    private GenericNotificationProducer producer;
    private SimplePausableSubscriptionManager manager;

    private SoapForwardingHub myHub;

    private final String producerEndpoint = "myProducer";
    private final String managerEndpoint = "myManager";

    /**
     * Everything that needs to be done to set the system up can be done in the constructor.
     */
    public ProducerWithSubscriptionManager() {
        // Instantiate the Web Services
        producer = new GenericNotificationProducer();

        // We are using a pausable subscription manager here. A regular one could be added in the same fashion
        manager = new SimplePausableSubscriptionManager();
        // This makes our manager update itself every 60 seconds, checking for expired subscriptions
        manager.setScheduleInterval(60);

        // Quick build the Web services, thus initializing the entire system
        myHub = producer.quickBuild(producerEndpoint);
        manager.quickBuild(managerEndpoint, myHub);


        // This is the crucial part, we have to add the manager to the producer. If not, the producer
        // will not be able to receive events when a subscription is changed at the manager
        producer.setSubscriptionManager(manager);

        // And we are done really. Obviously you can at this stage start creating filters and similar voodoo. But this will
        // not be covered here.
    }
}
