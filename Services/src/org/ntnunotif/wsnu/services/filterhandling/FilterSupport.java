package org.ntnunotif.wsnu.services.filterhandling;

import com.google.common.collect.ImmutableMap;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Inge on 02.04.2014.
 */
public class FilterSupport {

    /**
     * The <code>SubscriptionInfo</code> should contain information about a subscription.
     */

    public static final class SubscriptionInfo {
        public static final SubscriptionInfo DEFAULT_FILTER_SUPPORT;

        private final ImmutableMap<QName, Object> filters;

        /**
         * Run once on class load to initialize class correctly
         */
        static {
            // Filter names
            QName topicExpressionName = new QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpression", "wsnt");
            QName producerPropName = new QName("http://docs.oasis-open.org/wsn/b-2", "ProducerProperties", "wsnt");
            QName messageContentName = new QName("http://docs.oasis-open.org/wsn/b-2", "MessageContent", "wsnt");

            // defaults source map
            HashMap<QName, Object> defaults = new HashMap<>();

            // Fill map
            defaults.put(topicExpressionName, new TopicExpressionType());
            defaults.put(producerPropName, new QueryExpressionType());
            defaults.put(messageContentName, new QueryExpressionType());

            // Create DEFAULT_FILTER_SUPPORT
            DEFAULT_FILTER_SUPPORT = new SubscriptionInfo(defaults);
        }

        public SubscriptionInfo(Map<QName, Object> filtersIncluded) {
            this.filters = ImmutableMap.copyOf(filtersIncluded);
        }

        public boolean usesFilter(QName filterName) {
            return filters.containsKey(filterName);
        }

        public Class getFilterClass(QName filterName) {
            return filters.get(filterName).getClass();
        }

        public Object getFilter(QName filterName) {
            return filters.get(filterName);
        }

        public Set<QName> getFilterSet() {
            return filters.keySet();
        }
    }


    private Map<QName, FilterEvaluator> evaluatorMap = new HashMap<>();

    public static FilterSupport createDefaultFilterSupport() {
        FilterSupport fs = new FilterSupport();

        DefaultTopicExpressionFilterEvaluator topiEval = new DefaultTopicExpressionFilterEvaluator();
        DefaultProducerPropertiesFilterEvaluator prodEval = new DefaultProducerPropertiesFilterEvaluator();
        DefaultMessageContentFilterEvaluator messEval = new DefaultMessageContentFilterEvaluator();

        fs.setFilterEvaluator(topiEval);
        fs.setFilterEvaluator(prodEval);
        fs.setFilterEvaluator(messEval);

        return fs;
    }

    public FilterEvaluator getFilterEvaluator(QName filterName) {
        return evaluatorMap.get(filterName);
    }

    public void setFilterEvaluator(FilterEvaluator evaluator) {
        evaluatorMap.put(evaluator.filterName(), evaluator);
    }

    public boolean supportsFilter(QName filter) {
        return evaluatorMap.containsKey(filter);
    }

    public Class getFilterClass(QName filterName) {
        return evaluatorMap.get(filterName).filterClass();
    }

    public Notify evaluateNotifyToSubscription(Notify notify, FilterSupport.SubscriptionInfo subscriptionInfo) {
        // Tries not to destroy source Notify
        Notify returnValue = ServiceUtilities.cloneNotifyShallow(notify);

        // Do a check on all filters, to see if the filter at at least one instance evaluates to false
        for (QName fName: subscriptionInfo.getFilterSet()) {
            returnValue = evaluatorMap.get(fName).evaluate(returnValue, subscriptionInfo.usesFilter(fName));
        }

        return returnValue;
    }
}
