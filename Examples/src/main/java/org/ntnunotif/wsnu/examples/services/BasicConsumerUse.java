package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.topics.TopicValidator;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * A very simple example showing just haw easy a consumer may be built.
 */
public class BasicConsumerUse {

    public static void main(String[] args) {
        // Disable logging
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        // This example shows just how simple it is to create a Consumer.

        // First you need an instance of the NotificationConsumer class. This will be the class you interact with to
        // register with producers/ brokers, and where you tell your program where to send the notify messages.
        NotificationConsumer notificationConsumer = new NotificationConsumer();

        // We would also need to know its endpoint reference (the part directly after our root address)
        final String endpoint = "exampleConsumerEndpoint";

        // To get it up and able to receive Notify messages, it needs to be registered with a hub that can direct it
        // onto the Internet. The simplest way of doing this would be to run the quickBuild command. The returned hub is
        // there to enable multiple Web Services on one hub.
        SoapForwardingHub hub = notificationConsumer.quickBuild(endpoint);

        // Well, at this point, the consumer can receive notifies. But nothing happens to them. To change this, we need
        // to add a listeners that listens for notifies. Let us make a anonymous inner class that listens to these
        // events.
        notificationConsumer.addConsumerListener(new ConsumerListener() {

            @Override
            public void notify(NotificationEvent event) {
                // Here we can do anything with the event. Or nothing if we choose to. To just show we got a message:
                System.out.println("Received notify!");
                System.out.println("\tNumber of messages in the notify: " + event.getMessage().size());


                // To do something more interesting, loop through the messages received, and write out which topics
                // they were on (if any).
                for (NotificationMessageHolderType holderType : event.getRaw().getNotificationMessage()) {

                    TopicExpressionType topic = holderType.getTopic();

                    if (topic == null) {
                        // this message did not have a topic
                        System.out.println("\tNO TOPIC on this message");
                    } else {
                        // Print out the topic
                        try {

                            // The event holds the request information, which stores the context it stands in
                            NamespaceContext namespaceContext = event.getRequestInformation().getNamespaceContext(topic);
                            // The context is needed to understand which topic it was
                            List<QName> topicAsList = TopicValidator.evaluateTopicExpressionToQName(topic, namespaceContext);

                            // A topic may be represented as a String
                            String topicAsString = TopicUtils.topicToString(topicAsList);
                            // And printed for our convenience
                            System.out.println("\tTOPIC: " + topicAsString);

                        } catch (InvalidTopicExpressionFault invalidTopicExpressionFault) {
                            // We could not understand the expression
                            System.out.println("\tWARNING a message with not understandable topic was received");
                        } catch (MultipleTopicsSpecifiedFault multipleTopicsSpecifiedFault) {
                            // The topic expression was evaluated to mean more than one topic
                            System.out.println("\tWARNING a message with multiple topics was received");
                        } catch (TopicExpressionDialectUnknownFault topicExpressionDialectUnknownFault) {
                            // We did not understand the dialect of the topic given
                            System.out.println("\tWARNING a message with an unknown dialect was presented");
                        }
                    }
                }
            }
        });

        // Well this is all well. But we still do not receive any notifies. The reason behind this is of course that no
        // producers knows we exists, and therefore do not know that we wish to receive any messages.

        // To fix this, we need to know where a producer is located, its complete address.
        // Let us just assume it is located here:
        String producerAddress = "http://localhost:8080/exampleProducer";
        // And now register us as consumers
        InternalMessage reply = notificationConsumer.sendSubscriptionRequest(producerAddress);

        // We expect the request to fail, there are no producers on this address:
        if ((reply.statusCode & InternalMessage.STATUS_FAULT) != 0) {
            System.out.println("We failed to generate a subscription, as expected");
        } else {
            System.err.println("The subscription succeeded when it should not");
        }

        // And we should now get the notifies, if the producer existed
        System.exit(0);
    }
}
