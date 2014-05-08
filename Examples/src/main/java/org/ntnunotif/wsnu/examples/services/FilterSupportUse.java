package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.topics.SimpleEvaluator;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.filterhandling.DefaultTopicExpressionFilterEvaluator;
import org.ntnunotif.wsnu.services.filterhandling.FilterEvaluator;
import org.ntnunotif.wsnu.services.filterhandling.FilterSupport;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.GenericNotificationProducer;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

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

        // Now this support is set up. To use it with a GenericNotificationProducer, add it in the producers constructor.
        // The example producer does not need to support GetCurrentMessage caching.
        GenericNotificationProducer producer = new GenericNotificationProducer(filterSupport, false);
        // Now you may run the quickbuild, and the producer is ready to go.

        //producer.quickBuild("someEndpointReference");




        // If, for some reason, you need to use the filter support in a producer or similar thing you are making
        // yourself, some basics are covered below. If you need more information, I would encourage you to look into the
        // code of GenericNotificationProducer to see how it is used there.

        // code setting up a filter
        TopicExpressionType topicExpressionFilter = new TopicExpressionType();
        topicExpressionFilter.setDialect(SimpleEvaluator.dialectURI);
        topicExpressionFilter.getContent().add("ns:root");

        // TODO Finish
    }
}
