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

package org.ntnunotif.wsnu.services.filterhandling;

import com.google.common.collect.ImmutableMap;
import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.general.WsnUtilities;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The <code>FilterSupport</code> is a class that is designed to help with filtering in producers and similar places.
 */
public class FilterSupport {

    /**
     * The <code>SubscriptionInfo</code> should contain information about a subscription.
     */

    public static final class SubscriptionInfo {
        /**
         * The information about which filters is supported in the default <code>FilterSupport</code>
         */
        public static final SubscriptionInfo DEFAULT_FILTER_SUPPORT;

        private final ImmutableMap<QName, Object> filters;

        /**
         * The {@link org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver} responsible for
         * solving {@link javax.xml.namespace.NamespaceContext}s in a subscription.
         */
        public final NuNamespaceContextResolver namespaceContextResolver;

        /**
         * Run once on class load to initialize class correctly
         */
        static {
            // Filter names
            QName topicExpressionName = new QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpression", "wsnt");
            //QName producerPropName = new QName("http://docs.oasis-open.org/wsn/b-2", "ProducerProperties", "wsnt");
            QName messageContentName = new QName("http://docs.oasis-open.org/wsn/b-2", "MessageContent", "wsnt");

            // defaults source map
            HashMap<QName, Object> defaults = new HashMap<>();

            // Fill map
            defaults.put(topicExpressionName, new TopicExpressionType());
            //defaults.put(producerPropName, new QueryExpressionType());
            defaults.put(messageContentName, new QueryExpressionType());

            // Create DEFAULT_FILTER_SUPPORT
            DEFAULT_FILTER_SUPPORT = new SubscriptionInfo(defaults, null);
        }

        /**
         * Creates a <code>SubscriptionInfo</code> with an immutable copy of the filters that are included.
         *
         * @param filtersIncluded          the filters that exists in the subscription
         * @param namespaceContextResolver the object responsible for solving namespaces for the filters in the subscription
         */
        public SubscriptionInfo(Map<QName, Object> filtersIncluded, NuNamespaceContextResolver namespaceContextResolver) {
            if (filtersIncluded == null) {
                this.filters = ImmutableMap.of();
            } else {
                this.filters = ImmutableMap.copyOf(filtersIncluded);
            }
            this.namespaceContextResolver = namespaceContextResolver;
        }

        /**
         * Uses this subscription the filter identified with the given name.
         *
         * @param filterName the name of the filter (qualified tag name)
         * @return if this subscription uses this filter
         */
        public boolean usesFilter(QName filterName) {
            return filters.containsKey(filterName);
        }

        /**
         * Gets the class representing the filter with the given name.
         *
         * @param filterName the name of the filter (qualified tag name)
         * @return the class of the filter object.
         */
        public Class getFilterClass(QName filterName) {
            return filters.get(filterName).getClass();
        }

        /**
         * Gets the filter with the given name.
         *
         * @param filterName the name of the filter (qualified tag name)
         * @return the filter itself
         */
        public Object getFilter(QName filterName) {
            return filters.get(filterName);
        }

        /**
         * Gets the {@link java.util.Set} of the qualified names of the filters in this subscription.
         *
         * @return the set containing all the names.
         */
        public Set<QName> getFilterSet() {
            return filters.keySet();
        }
    }


    private Map<QName, FilterEvaluator> evaluatorMap = new HashMap<>();

    /**
     * Creates a <code>FilterSupport</code> with support for TopicFilter and MessageContentFilter.
     *
     * @return a fully usable filter support with the given filters included.
     */
    public static FilterSupport createDefaultFilterSupport() {
        FilterSupport fs = new FilterSupport();

        DefaultTopicExpressionFilterEvaluator topiEval = new DefaultTopicExpressionFilterEvaluator();
        //DefaultProducerPropertiesFilterEvaluator prodEval = new DefaultProducerPropertiesFilterEvaluator();
        DefaultMessageContentFilterEvaluator messEval = new DefaultMessageContentFilterEvaluator();

        fs.setFilterEvaluator(topiEval);
        //fs.setFilterEvaluator(prodEval);
        fs.setFilterEvaluator(messEval);

        return fs;
    }

    /**
     * Gets the evaluator registered for the filter identified with the name given.
     *
     * @param filterName the name of the filter (qualified tag name)
     * @return the {@link org.ntnunotif.wsnu.services.filterhandling.FilterEvaluator} used by this <code>FilterSupport</code>
     */
    public FilterEvaluator getFilterEvaluator(QName filterName) {
        return evaluatorMap.get(filterName);
    }

    /**
     * Sets an evaluator. It will give the name it should be registered to itself.
     *
     * @param evaluator the {@link org.ntnunotif.wsnu.services.filterhandling.FilterEvaluator} to register.
     */
    public void setFilterEvaluator(FilterEvaluator evaluator) {
        evaluatorMap.put(evaluator.filterName(), evaluator);
    }

    /**
     * Checks if this <code>FilterSupport</code> supports the filter given. The legality of the filter will be checked
     * against the filter evaluator registered for this filter.
     *
     * @param filterName    the name of the filter (qualified tag name)
     * @param filter        the filter itself
     * @param filterContext the {@link javax.xml.namespace.NamespaceContext} of the filter
     * @return <code>true</code> if filter is ok. <code>false</code> otherwise
     * @throws TopicExpressionDialectUnknownFault   if the filter was a TopicExpressionFilter, but the dialect was unknown
     * @throws InvalidTopicExpressionFault          if the filter was a TopicExpressionFilter, but the filter content was malformed
     * @throws InvalidMessageContentExpressionFault if the filter was a MessageContentFilter, but something in it was malformed
     */
    public boolean supportsFilter(QName filterName, Object filter, NamespaceContext filterContext) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault, InvalidMessageContentExpressionFault {

        // check if we actually have a delegate to check filter against
        if (evaluatorMap.containsKey(filterName)) {
            FilterEvaluator evaluator = evaluatorMap.get(filterName);

            // Check if the evaluator is for the correct filter class
            if (evaluator.filterClass().equals(filter.getClass())) {

                // Delegate to the evaluator to see if the filter actually is well formed
                return evaluator.isWellFormed(filter, filterContext);
            }
        }
        return false;
    }

    /**
     * Evaluates a notify against a subscription. The notify itself will not be altered, but shallowly cloned.
     *
     * @param notify                   the notify to perform filtering on
     * @param subscriptionInfo         the information about the subscription
     * @param namespaceContextResolver the object responsible for solving namespaces for the notify
     * @return the filtered notify with equal or less amount of messages or <code>null</code> if no messages were accepted
     */
    public Notify evaluateNotifyToSubscription(Notify notify, FilterSupport.SubscriptionInfo subscriptionInfo,
                                               NuNamespaceContextResolver namespaceContextResolver) {
        Log.d("FilterSupport", "Evaluating notify with number of messages: " + notify.getNotificationMessage().size());
        // Tries not to destroy source Notify
        Notify returnValue = WsnUtilities.cloneNotifyShallow(notify);

        // Do a check on all filters, to see if the filter at at least one instance evaluates to false
        for (QName fName : subscriptionInfo.getFilterSet()) {
            Object filter = subscriptionInfo.getFilter(fName);
            returnValue = evaluatorMap.get(fName).evaluate(returnValue, namespaceContextResolver,
                    filter, subscriptionInfo.namespaceContextResolver.resolveNamespaceContext(filter));
        }

        Log.d("FilterSupport", "Returning evaluated notifies: " +
                (returnValue == null ? null : returnValue.getNotificationMessage().size()));

        return returnValue;
    }
}
