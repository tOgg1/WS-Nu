package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.services.implementations.notificationbroker.GenericNotificationBroker;

/**
 * Example showing very basic use of the NotificationBroker. This example will not go detailed into how
 * filters etc. are created. This information can be found in {@link org.ntnunotif.wsnu.examples.services.BasicProducerUse}.
 */
public class MinimalisticBroker {

    private GenericNotificationBroker broker;

    private final String brokerEndpoint = "myBroker";

    private SoapForwardingHub myHub;
    /**
     * We can do everything needed in the constructor
     */
    public MinimalisticBroker() {
        // Instantiate the broker
        broker = new GenericNotificationBroker();

        // By calling the brokers quickbuild method, we are starting both the broker and the rest of the
        // system, all in one.
        myHub = broker.quickBuild(brokerEndpoint);

        // This method makes our Broker not demand that a publisher publishing on the broker
        // is registered with the broker beforehand
        broker.setDemandRegistered(false);

        // This method makes our broker cache messages, for use with the getCurrentMessage web method.
        broker.setCacheMessages(true);

        // This is the most minimalistic example of creating a NotificationBroker possible. For more information
        // on filtersupport etc. see the BasicProducerUse example
    }
}
