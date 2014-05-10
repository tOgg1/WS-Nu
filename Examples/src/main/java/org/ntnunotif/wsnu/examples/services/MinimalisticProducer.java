package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.GenericNotificationProducer;

/**
 * A minimalistic producer example.
 */
public class MinimalisticProducer {

    private GenericNotificationProducer producer;

    private final String producerEndpoint = "myProducer";

    private SoapForwardingHub myHub;

    /**
     * Everything that needs to be done to set the system up can be done in the constructor.
     */
    public MinimalisticProducer() {
        // Instantiate the producer
        producer = new GenericNotificationProducer();

        // By calling the producers quickbuild method, we are starting both the producer and the rest of the
        // system, all in one.
        myHub = producer.quickBuild(producerEndpoint);

        // This is all that is strictly necessary to have your producer up and running. For a slightly more advanced example
        // see "BasicProducerUse"

    }
}
