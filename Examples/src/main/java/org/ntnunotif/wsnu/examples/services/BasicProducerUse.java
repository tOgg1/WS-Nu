package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.base.topics.SimpleEvaluator;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.GenericNotificationProducer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

/**
 * A very simple example showing just how easy a producer may be built.
 */
public class BasicProducerUse {

    public static void main(String[] args) {
        // Disable logging
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        // This example shows how simple it is to create a producer that may distribute notifies.

        // First you need an instance of the GenericNotificationProducer class. This will serve as the delegate to
        // distribute notifications. If you wish, you may extend the class, though we do not do that here.
        GenericNotificationProducer producer = new GenericNotificationProducer();

        // This producer is now set up (by default) to support GetCurrentMessage on a specific topic, and it support
        // filtering on topics and message content.

        // We would also need to know its endpoint reference (the part directly after our root address)
        final String endpoint = "exampleProducerEndpoint";

        // To get it up and able to send Notify messages, it needs to be registered with a hub that can direct it
        // onto the Internet. The simplest way of doing this would be to run the quickBuild command. The returned hub is
        // there to enable multiple Web Services on one hub.
        SoapForwardingHub hub = producer.quickBuild(endpoint);

        // At this point consumers are able to register subscriptions to our producer (as long as they know it exists).

        // To send a notify which is filtered on the given filters, we need to build it and its context.
        NotifyWithContext notifyWithContext = buildNotifyWithContext();

        // Finally, to send the notify to all who wishes to see it, we only need to run
        producer.sendNotification(notifyWithContext.notify, notifyWithContext.nuNamespaceContextResolver);
    }

    /**
     * This method is a helper method to build a Notify with its context. It is meant as example on how this may be
     * solved.
     *
     * @return a notify with its context
     */
    public static NotifyWithContext buildNotifyWithContext() {
        // Prefix and namespace used in topics
        final String prefix = "ens";
        final String namespace = "http://www.example.com/producerns";

        // Create a contextResolver, and fill it with the namespace bindings used in the notify
        NuNamespaceContextResolver contextResolver = new NuNamespaceContextResolver();
        contextResolver.openScope();
        contextResolver.putNamespaceBinding(prefix, namespace);

        // Build the notify
        ObjectFactory factory = new ObjectFactory();
        Notify notify = factory.createNotify();

        // Fill it with some messages with topics
        for (int i = 0; i  < 5; i++) {
            // Create message and holder
            NotificationMessageHolderType.Message message = factory.createNotificationMessageHolderTypeMessage();
            NotificationMessageHolderType messageHolderType = factory.createNotificationMessageHolderType();

            // Set message context
            message.setAny("message " + i);

            // Set holders message
            messageHolderType.setMessage(message);

            // Build topic expression
            String expression = prefix + ":root-" + (i % 3);
            // Build topic
            TopicExpressionType topicExpressionType = factory.createTopicExpressionType();
            topicExpressionType.setDialect(SimpleEvaluator.dialectURI);
            topicExpressionType.getContent().add(expression);

            messageHolderType.setTopic(topicExpressionType);

            // remember to bind the necessary objects to the context
            contextResolver.registerObjectWithCurrentNamespaceScope(message);
            contextResolver.registerObjectWithCurrentNamespaceScope(topicExpressionType);

            // Add message to the notify
            notify.getNotificationMessage().add(messageHolderType);
        }

        // ready for return
        contextResolver.closeScope();
        NotifyWithContext notifyWithContext = new NotifyWithContext();
        notifyWithContext.notify = notify;
        notifyWithContext.nuNamespaceContextResolver = contextResolver;

        return notifyWithContext;
    }

    /**
     * A wrapper class to hold a notify with its context.
     */
    public static class NotifyWithContext {
        public Notify notify;
        public NuNamespaceContextResolver nuNamespaceContextResolver;
    }
}
