//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.base.topics.SimpleEvaluator;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.filterhandling.DefaultTopicExpressionFilterEvaluator;
import org.ntnunotif.wsnu.services.filterhandling.FilterEvaluator;
import org.ntnunotif.wsnu.services.filterhandling.FilterSupport;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.NotificationProducerImpl;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * An example on how to use the basic functionality in FilterSupport
 */
public class FilterSupportUse {

    public static void main(String[] args) {
        // Disable logging
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        // This example will focus on how you may use the filter support for different tasks.
        // How filter support may be extended, is described in FilterSupportExtension.

        // Let us for the first point assume you want a producer that only filters on topics. This is easy to do.
        // First you create a FilterSupport without any predefined filters.
        FilterSupport filterSupport = new FilterSupport();
        // Register the filter evaluators we wish to use
        FilterEvaluator evaluator = new DefaultTopicExpressionFilterEvaluator();
        filterSupport.setFilterEvaluator(evaluator);

        // Now this support is set up. To use it with a NotificationProducerImpl, add it in the producers constructor.
        // The example producer does not need to support GetCurrentMessage caching.
        NotificationProducerImpl producer = new NotificationProducerImpl();
        producer.setFilterSupport(filterSupport);
        producer.setCacheMessages(false);
        // Now you may run the quickbuild, and the producer is ready to go.

        //producer.quickBuild("someEndpointReference");

        // If, for some reason, you need to use the filter support in a producer or similar thing you are making
        // yourself, some basics are covered below. If you need more information, I would encourage you to look into the
        // code of NotificationProducerImpl to see how it is used there.

        // code setting up a filter
        TopicExpressionType topicExpressionFilter = new TopicExpressionType();
        topicExpressionFilter.setDialect(SimpleEvaluator.dialectURI);
        topicExpressionFilter.getContent().add("ns:root");

        // Setting up a namespace context resolver for the filter
        NuNamespaceContextResolver filterResolver = new NuNamespaceContextResolver();
        filterResolver.openScope();
        filterResolver.putNamespaceBinding("ns", "http://www.example.com");
        filterResolver.registerObjectWithCurrentNamespaceScope(topicExpressionFilter);
        filterResolver.closeScope();

        // Creating a Notify
        Notify notify = new Notify();
        // Creating two messages for the notify
        NotificationMessageHolderType.Message message1 = new NotificationMessageHolderType.Message();
        NotificationMessageHolderType.Message message2 = new NotificationMessageHolderType.Message();

        message1.setAny("some content");
        message2.setAny("some content");

        NotificationMessageHolderType messageHolderType1 = new NotificationMessageHolderType();
        NotificationMessageHolderType messageHolderType2 = new NotificationMessageHolderType();

        messageHolderType1.setMessage(message1);
        messageHolderType2.setMessage(message2);

        TopicExpressionType topic1 = new TopicExpressionType();
        topic1.setDialect(SimpleEvaluator.dialectURI);
        topic1.getContent().add("ns1:root");

        TopicExpressionType topic2 = new TopicExpressionType();
        topic2.setDialect(SimpleEvaluator.dialectURI);
        topic2.getContent().add("ns2:root");

        messageHolderType1.setTopic(topic1);
        messageHolderType2.setTopic(topic2);

        // adding the messages to the notify
        notify.getNotificationMessage().add(messageHolderType1);
        notify.getNotificationMessage().add(messageHolderType2);

        // Set up the necessary context for the notify
        NuNamespaceContextResolver notifyContext = new NuNamespaceContextResolver();
        notifyContext.openScope();
        notifyContext.putNamespaceBinding("ns1", "http://www.example.com");
        notifyContext.putNamespaceBinding("ns2", "http://www.example.com/fail");
        notifyContext.registerObjectWithCurrentNamespaceScope(topic1);
        notifyContext.registerObjectWithCurrentNamespaceScope(topic2);

        // Show that there are actually two messages in the notify
        if (notify.getNotificationMessage().size() != 2) {
            System.err.println("There were not two messages here");
        }

        // And now, to demonstrate how you evaluate, create a subscription info to be used
        // A map showing which filters we should evaluate with
        Map<QName, Object> filterMap = new HashMap<>();
        // The filterMap must map the qualified name of the filter to the filter
        filterMap.put(new QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpression"), topicExpressionFilter);

        FilterSupport.SubscriptionInfo subscriptionInfo = new FilterSupport.SubscriptionInfo(filterMap, filterResolver);

        // The notify we created may now be filtered with the filter we created
        Notify filteredNotify = filterSupport.evaluateNotifyToSubscription(notify, subscriptionInfo, notifyContext);

        // The notify now only contains one message
        if (filteredNotify.getNotificationMessage().size() != 1) {
            System.err.println("The messages was not filtered");
        }

        // If is not the same notify:
        if (notify == filteredNotify) {
            System.err.println("The notifies was the same object");
        }

        // They do not contain the same amount of messages
        if (notify.getNotificationMessage().size() == filteredNotify.getNotificationMessage().size()) {
            System.err.println("The notifies contained the same amount of messages");
        }
    }
}
