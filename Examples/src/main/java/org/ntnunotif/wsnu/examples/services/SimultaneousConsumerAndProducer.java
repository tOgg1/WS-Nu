package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.GenericNotificationProducer;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * This example shows how to build two simple consumers and one producer. For the sake of the example, they run on the
 * same computer. The example assumes the {@link org.ntnunotif.wsnu.examples.services.BasicConsumerUse} and
 * the {@link org.ntnunotif.wsnu.examples.services.BasicProducerUse} examples have been read and understood.
 */
public class SimultaneousConsumerAndProducer {

    private static final String PRODUCER_ENDPOINT = "exampleProducer";
    private static final String CONSUMER_1_ENDPOINT = "exampleConsumer1";
    private static final String CONSUMER_2_ENDPOINT = "exampleConsumer2";

    public static void main(String[] args) {
        // Disable logging
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        // Start with building the producer (the producer need to run before any consumers may be registered to get
        // notifies)

        GenericNotificationProducer producer = new GenericNotificationProducer();
        // We need to remember the hub so that we can use it for the consumers
        SoapForwardingHub hub = producer.quickBuild(PRODUCER_ENDPOINT);

        // This is the address we will be using for the producer:
        String producerAddress = "http://127.0.0.1:8080/" + PRODUCER_ENDPOINT;

        // Sleep a little, so the example do not finish at once
        try {
            System.out.println("The producer is built, sleep");
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        // Create the first consumer
        NotificationConsumer consumer1 = new NotificationConsumer();
        // The quickBuild takes in the hub we are should connect to
        consumer1.quickBuild(CONSUMER_1_ENDPOINT, hub);
        consumer1.addConsumerListener(new PrintConsumerListener("First Consumer"));

        // The endpoint reference of the consumer
        String consumer1Endpoint = "http://127.0.0.1:8080/" + CONSUMER_1_ENDPOINT;

        // Register the first consumer to listen to notifications from the producer
        Subscribe subscribe = buildSubscriptionRequest("root-0", consumer1Endpoint);
        InternalMessage subscribeResponse = consumer1.sendSubscriptionRequest(subscribe, producerAddress);

        // Did everything go right?
        if ((subscribeResponse.statusCode & InternalMessage.STATUS_FAULT) == 0) {
            System.out.println("First consumer was correctly added");
        } else {
            System.err.println("First consumer failed to subscribe");
        }

        // First consumer finished, sleep a little
        try {
            System.out.println("First consumer registered, sleep");
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }



        // Create the second consumer
        NotificationConsumer consumer2 = new NotificationConsumer();
        // The quickBuild takes in the hub we are should connect to
        consumer2.quickBuild(CONSUMER_2_ENDPOINT, hub);
        consumer2.addConsumerListener(new PrintConsumerListener("Second Consumer"));

        // The endpoint reference of the consumer
        String consumer2Endpoint = "http://127.0.0.1:8080/" + CONSUMER_2_ENDPOINT;

        // Register the first consumer to listen to notifications from the producer
        subscribe = buildSubscriptionRequest("root-2", consumer2Endpoint);
        subscribeResponse = consumer1.sendSubscriptionRequest(subscribe, producerAddress);

        // Did everything go right?
        if ((subscribeResponse.statusCode & InternalMessage.STATUS_FAULT) == 0) {
            System.out.println("Second consumer was correctly added");
        } else {
            System.err.println("Second consumer failed to subscribe");
        }

        // Second consumer finished, sleep a little
        try {
            System.out.println("Second consumer registered, sleep");
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        // Try to send a notify
        BasicProducerUse.NotifyWithContext notifyWithContext = BasicProducerUse.buildNotifyWithContext();
        producer.sendNotification(notifyWithContext.notify, notifyWithContext.nuNamespaceContextResolver);

        // Sleep a little, so we know the system has time to print responses before exiting
        try {
            System.out.println("Notify sent, sleep");
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        System.out.println("Woken up. Exiting");
        System.exit(0);
    }

    public static Subscribe buildSubscriptionRequest(String topicLocalName, String endpointReference) {
        // create the object
        ObjectFactory factory = new ObjectFactory();
        Subscribe subscribe = factory.createSubscribe();

        // Create the topic. This is a convenient way of building topic expressions that is very usable for requests
        List<QName> topicList = new ArrayList<>();
        QName topicName = new QName(BasicProducerUse.namespace, topicLocalName, BasicProducerUse.prefix);
        topicList.add(topicName);
        TopicExpressionType topic = TopicUtils.translateQNameListTopicToTopicExpression(topicList);

        // Create the filter
        FilterType filterType = factory.createFilterType();
        // Notice in particular which tag the topicExpression is marked with
        filterType.getAny().add(factory.createTopicExpression(topic));

        // Set the filter on the subscribe message
        subscribe.setFilter(filterType);

        // Set where the notifies should be sent, using helper ins ServiceUtilities
        subscribe.setConsumerReference(ServiceUtilities.buildW3CEndpointReference(endpointReference));

        return subscribe;
    }

    /**
     * A helper class, printing out which consumer got the notify, how many messages it contained and which topics they
     * have
     */
    public static class PrintConsumerListener extends BasicConsumerUse.SimpleConsumerListener {
        private final String consumerName;

        public PrintConsumerListener(String consumerName) {
            this.consumerName = consumerName;
        }

        @Override
        public void notify(NotificationEvent event) {
            System.out.println();
            System.out.println();
            System.out.println("CONSUMER " + consumerName + " WAS NOTIFIED:");
            super.notify(event);
        }
    }
}
