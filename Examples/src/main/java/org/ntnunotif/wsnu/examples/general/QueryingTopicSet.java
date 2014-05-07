package org.ntnunotif.wsnu.examples.general;

import org.ntnunotif.wsnu.base.net.NuNamespaceContext;
import org.ntnunotif.wsnu.base.topics.*;
import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicSetType;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * This example shows how you can use the topic sets to query against. This example code is dependent on the example code in BuildingTopicSet.
 * Created by Inge on 07.05.2014.
 */
public class QueryingTopicSet {
    public static final String NAMESPACE_PREFIX_1 = "tns1";
    public static final String NAMESPACE_PREFIX_2 = "tns2";
    public static final String UNKNOWN_PREFIX = "unknown";

    public static void main(String[] args) {
        // Disable logging
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        // First we need a topic set. We have a method that builds one in the BuildingTopicSet example
        TopicSetType topicSetType = BuildingTopicSet.buildTopicSet();

        // We also need a NamespaceContext we will use in this example
        NuNamespaceContext namespaceContext = new NuNamespaceContext();
        // Let us populate the context
        namespaceContext.put(NAMESPACE_PREFIX_1, BuildingTopicSet.TOPIC_NAMESPACE_1);
        namespaceContext.put(NAMESPACE_PREFIX_2, BuildingTopicSet.TOPIC_NAMESPACE_2);

        // We then need to create some queries. For this, use the TopicExpressionType
        org.oasis_open.docs.wsn.b_2.ObjectFactory factory = new org.oasis_open.docs.wsn.b_2.ObjectFactory();
        TopicExpressionType simpleExpressionType1 = factory.createTopicExpressionType();
        TopicExpressionType simpleExpressionType2 = factory.createTopicExpressionType();
        TopicExpressionType concreteExpressionType = factory.createTopicExpressionType();
        TopicExpressionType illegalConcreteExpressionType = factory.createTopicExpressionType();
        TopicExpressionType xPathExpressionType = factory.createTopicExpressionType();

        // A query in the simple dialect is on the form namespace:root_topic. Let us make one we know should not return any results
        String queryExpression = NAMESPACE_PREFIX_1 + ":" + BuildingTopicSet.NON_TOPIC;
        // Fill in the actual expression:
        // The dialect is the simple dialect.
        simpleExpressionType1.setDialect(SimpleEvaluator.dialectURI);
        simpleExpressionType1.getContent().add(queryExpression);

        queryExpression = BuildingTopicSet.ROOT_TOPIC_1;
        simpleExpressionType2.setDialect(SimpleEvaluator.dialectURI);
        simpleExpressionType2.getContent().add(queryExpression);

        // A query in the concrete dialect is in the form (namespace:)root/((namespace:)child)*
        queryExpression = NAMESPACE_PREFIX_2 + ":" + BuildingTopicSet.ROOT_TOPIC_1 + "/";
        queryExpression += NAMESPACE_PREFIX_1 + ":" + BuildingTopicSet.CHILD_TOPIC_1 + "/";
        queryExpression += BuildingTopicSet.NON_TOPIC + "/";
        queryExpression += NAMESPACE_PREFIX_2 + ":" + BuildingTopicSet.CHILD_TOPIC_1;

        // Fill in the actual expression
        concreteExpressionType.setDialect(ConcreteEvaluator.dialectURI);
        concreteExpressionType.getContent().add(queryExpression);

        // One of the ways to make an illegal expression, is to use prefixes that are unbound in the namespace context
        queryExpression = UNKNOWN_PREFIX + ":" + BuildingTopicSet.ROOT_TOPIC_1;
        illegalConcreteExpressionType.setDialect(ConcreteEvaluator.dialectURI);
        illegalConcreteExpressionType.getContent().add(queryExpression);

        // Full dialect and XPath dialect can select multiple topics in a topic set. Let us create a XPath expression
        // that selects all nodes
        queryExpression = "//*";
        xPathExpressionType.setDialect(XPathEvaluator.dialectURI);
        xPathExpressionType.getContent().add(queryExpression);

        // We use the TopicValidator to get the intersection between the expression and the set
        try {
            TopicSetType resultSet = TopicValidator.getIntersection(simpleExpressionType1, topicSetType, namespaceContext);

            // This set should be empty, and this is represented by the fact that it is null
            if (resultSet != null) {
                System.err.println("The resulting set should be null");
            }

            // The second query on the other hand, should return a set containing exactly 1 topic
            resultSet = TopicValidator.getIntersection(simpleExpressionType2, topicSetType, namespaceContext);

            if (resultSet == null || TopicUtils.topicSetToQNameList(resultSet, true).size() != 1) {
                BuildingTopicSet.printTopicSetType(resultSet);
                System.err.println("The resulting set should contain exactly 1 topic, but did not");
            }

            // The legal concrete expression should select exactly one topic
            resultSet = TopicValidator.getIntersection(concreteExpressionType, topicSetType, namespaceContext);

            // this result should have results
            if (resultSet != null) {
                // Let us translate this set to a more easily manageable form, and look a little closer on it.
                List<List<QName>> resultAsLists = TopicUtils.topicSetToQNameList(resultSet, true);

                // The result should have one list
                if (resultAsLists.size() != 1) {
                    System.err.println("The result set should contain exactly 1 topic, but contained: " +
                            resultAsLists.size());
                } else {
                    // This list should have exactly four levels of QNames
                    if (resultAsLists.get(0).size() != 4) {
                        System.err.println("The resulting topic should be nested 4 times, but was nested: " +
                                resultAsLists.get(0).size());
                    }
                }
            } else {
                System.err.println("The resulting set should have one topic");
            }

            // The XPath expression should on the other hand select ALL topics (namely 8).
            resultSet = TopicValidator.getIntersection(xPathExpressionType, topicSetType, namespaceContext);
            if (resultSet == null || TopicUtils.topicSetToQNameList(resultSet, true).size() != 8) {
                BuildingTopicSet.printTopicSetType(resultSet);
                System.err.println("The XPath expression selected wrong number of topics");
            }
        } catch (Exception e) {
            System.err.println("Querying with valid expressions should not cause exceptions");
        }

        // Querying with an illegal expression should force the evaluator to cast an exception
        try {
            TopicValidator.getIntersection(illegalConcreteExpressionType, topicSetType, namespaceContext);
            System.err.println("There should be cast an exception before this line");
        } catch (TopicExpressionDialectUnknownFault topicExpressionDialectUnknownFault) {
            System.err.println("Concrete dialect should be known to the evaluator");
        } catch (InvalidTopicExpressionFault invalidTopicExpressionFault) {
            System.out.println("Correct exception caught, its message is: " + invalidTopicExpressionFault.getMessage()
                    + "\nwhich should state something about the unknown prefix");
        }
    }
}
