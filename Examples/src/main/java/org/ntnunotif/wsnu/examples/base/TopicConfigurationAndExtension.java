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

package org.ntnunotif.wsnu.examples.base;

import org.ntnunotif.wsnu.base.topics.TopicExpressionEvaluatorInterface;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.topics.TopicValidator;
import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicNamespaceType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * An example showing how topic validation may be configured.
 */
public class TopicConfigurationAndExtension implements TopicExpressionEvaluatorInterface {

    private static final String DIALECT_URI = "http://www.example.com/phony_dialect";
    private static final QName ONLY_SUPPORTED_TOPIC = new QName("http://www.example.com", "phony_topic");
    private static final String ONLY_ACCEPTABLE_TOPIC_EXPRESSION = "I be phony";

    public static void main(String[] args) {
        // Disable logging
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        // --- !!! CONFIGURATION !!! ---

        // In simple and concrete dialects the productions for topic expressions always start with a QName.
        // However, there have been places where some people have wanted the ability to allow a preceding "/" before the
        // expression. The ability to configure the validator to do this has been included.

        // Right now a simple or concrete expression as "/tns:root" would cause an InvalidTopicExpressionException
        // To alter how topics with simple and concrete dialect is evaluated, run:
        TopicValidator.setSlashAsSimpleAndConcreteDialectStartAccepted(true);
        // Right now a simple or concrete expression as "/tns:root" will be valid, and would be equivalent with "tns:root"



        // --- !!! EXTENSION !!! ---

        // The ability to support additional dialects is included. To do this, you only need to implement the
        // TopicExpressionEvaluatorInterface, and register the evaluating object with the validator. This may also be
        // used to override a default evaluator, if the need to do that is there.
        TopicExpressionEvaluatorInterface evaluator = new TopicConfigurationAndExtension();
        TopicValidator.addTopicExpressionEvaluator(evaluator);

        // The topic validator now can evaluate topics with this dialect. To demonstrate:
        try {
            TopicExpressionType expressionType = new TopicExpressionType();
            expressionType.setDialect(DIALECT_URI);
            expressionType.getContent().add(ONLY_ACCEPTABLE_TOPIC_EXPRESSION);

            List<QName> topicAsList = TopicValidator.evaluateTopicExpressionToQName(expressionType, null);

            System.out.println("The topic identified was this:");
            System.out.println(TopicUtils.topicToString(topicAsList));

        } catch (Exception e) {
            // This should not happen
            System.err.println("No exceptions should be thrown here");
        }
    }

    @Override
    public String getDialectURIAsString() {

        // This is a MUST to implement. Without this the evaluator will never be called, and an exception may be thrown
        // if you try to register a evaluator returning a null value here.

        return DIALECT_URI;
    }

    @Override
    public boolean evaluateTopicWithExpression(TopicExpressionType topicExpressionType, TopicType topicType) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        // this method is only used in namespace resolution, and is not normally used in the current version of WS-Nu
        throw new UnsupportedOperationException("Namespace resolution not supported");
    }

    @Override
    public TopicSetType getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType, NamespaceContext namespaceContext) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        // This method should get the intersecting topics in a set and the expression. In this case we can see if this
        // is a legal expression

        boolean legal = isLegalExpression(topicExpressionType, namespaceContext);
        if (!legal) {
            TopicUtils.throwInvalidTopicExpressionFault("en", "This is not phony. Really not phony!");
        }

        // And then see if the set contains the allowed topic
        for (Object o : topicSetType.getAny()) {

            if (o instanceof Node) {
                Node node = (Node) o;

                if (TopicUtils.findElementWithNameAndNamespace(
                        node, ONLY_SUPPORTED_TOPIC.getLocalPart(), ONLY_SUPPORTED_TOPIC.getNamespaceURI()) != null) {

                    // The set contained the allowed topic! Hooray!
                    // Do so complete set can be built, build it and return
                    List<QName> topic = new ArrayList<>();
                    topic.add(ONLY_SUPPORTED_TOPIC);
                    List<List<QName>> topicList = new ArrayList<>();
                    topicList.add(topic);

                    return TopicUtils.qNameListListToTopicSet(topicList);
                }
            }

        }
        return null;
    }

    @Override
    public boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        // this method is only used in namespace resolution, and is not normally used in the current version of WS-Nu
        throw new UnsupportedOperationException("Namespace resolution not supported");
    }

    @Override
    public List<QName> evaluateTopicExpressionToQName(TopicExpressionType topicExpressionType, NamespaceContext context) throws
            UnsupportedOperationException, InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault, TopicExpressionDialectUnknownFault {

        // This method should try to evaluate a simple expression to a topic. In this case there exists only one
        // identifiable topic, and if the expression represents this, it will be returned

        if (isLegalExpression(topicExpressionType, context)) {
            List<QName> topic = new ArrayList<>();
            topic.add(ONLY_SUPPORTED_TOPIC);
            return topic;
        }

        // Else the expression was illegal, and an exception is thrown
        TopicUtils.throwInvalidTopicExpressionFault("en", "This was not a phony topic expression");
        return null;
    }

    @Override
    public boolean isLegalExpression(TopicExpressionType topicExpressionType, NamespaceContext namespaceContext) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        // This method should check if this expression is a legal expression in this dialect. If not, the implementation
        // can choose between returning false, or throwing the correct exception directly

        if (!DIALECT_URI.equals(topicExpressionType.getDialect())) {
            // Illegal dialect
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "the phony evaluator does not support this dialect");
        }

        // Extract the topic string from the expression
        String expressionString = TopicUtils.extractExpression(topicExpressionType);
        if (!ONLY_ACCEPTABLE_TOPIC_EXPRESSION.equals(expressionString)) {

            // It is not a legal expression. We can choose between throwing an exception or returning false

            if (Math.random() < 0.5) {
                TopicUtils.throwInvalidTopicExpressionFault("en", "Not phony!");
            } else {
                return false;
            }

        }

        return true;
    }
}
