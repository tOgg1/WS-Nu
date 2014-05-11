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

package org.ntnunotif.wsnu.base.topics;

import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicNamespaceType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>SimpleEvaluator</code> is an evaluator that support the simple dialect, defined by OASIS WS-Topics 1.3
 */
public class SimpleEvaluator implements TopicExpressionEvaluatorInterface {
    /**
     * The dialect this evaluator supports
     */
    public static final String dialectURI = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";

    @Override
    public String getDialectURIAsString() {
        return dialectURI;
    }

    @Override
    public boolean evaluateTopicWithExpression(TopicExpressionType topicExpressionType, TopicType topicType)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        Log.d("SimpleEvaluator", "evaluateTopicWithExpression called");
        throw new UnsupportedOperationException("Topic namespace not supported yet!");
    }

    @Override
    public TopicSetType getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType,
                                        NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        Log.d("SimpleEvaluator", "getIntersection called");

        if (!dialectURI.equals(topicExpressionType.getDialect()))
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "Simple evaluator can evaluate Simple dialect!");

        QName topic;
        try {
            topic = evaluateTopicExpressionToQName(topicExpressionType, namespaceContext).get(0);
            Log.d("SimpleEvaluator", "Expression QName: " + topic.toString());
        } catch (MultipleTopicsSpecifiedFault fault) {
            // This is impossible in simple dialect
            Log.e("SimpleEvaluator[Topic]", "A simple topic expression got evaluated to multiple topics. This " +
                    "should be impossible");
            fault.printStackTrace();
            return null;
        }

        TopicSetType retVal = new TopicSetType();
        for (Object o : topicSetType.getAny()) {

            if (o instanceof Node) {

                Node node = (Node) o;
                String nodeNS = node.getNamespaceURI();

                // Ensure last letter in namespace is not /
                if (nodeNS != null && nodeNS.length() != 0 && (nodeNS.charAt(nodeNS.length() - 1) == '/' || nodeNS.charAt(nodeNS.length() - 1) == ':')) {
                    nodeNS = nodeNS.substring(0, nodeNS.length()-1);
                }

                String nodeName = node.getLocalName() == null ? node.getNodeName() : node.getLocalName();
                boolean bothNSisNull = topic.getNamespaceURI() == null ||
                        topic.getNamespaceURI().equals(XMLConstants.NULL_NS_URI);
                bothNSisNull = bothNSisNull && (nodeNS == null || nodeNS.equals(XMLConstants.NULL_NS_URI));

                try {
                    Node addNode = null;
                    // The returned set should only contain a single topic node with only non-topic parents
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    Document owner = factory.newDocumentBuilder().newDocument();
                    Element firstChild = owner.createElement("SetRoot");

                    if (bothNSisNull) {
                        if (topic.getLocalPart().equals(nodeName) && TopicUtils.isTopic(node)) {
                            addNode = owner.importNode(node, false);
                            firstChild.appendChild(addNode);
                            retVal.getAny().add(addNode);
                            break;
                        }
                    } else {
                        String topicNS = topic.getNamespaceURI();
                        // Ensure topicNS does not end with /

                        if (topicNS != null && topicNS.length() != 0 && (topicNS.charAt(topicNS.length() - 1) == '/' || topicNS.charAt(topicNS.length() - 1) == ':')) {
                            topicNS = topicNS.substring(0, topicNS.length() - 1);
                        }

                        if (topicNS != null && topicNS.equals(nodeNS) &&
                                topic.getLocalPart().equals(nodeName) && TopicUtils.isTopic(node)) {
                            addNode = owner.importNode(node, false);
                            firstChild.appendChild(addNode);
                            retVal.getAny().add(addNode);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e("SimpleEvaluator", "Could not create a document factory");
                }
            }
        }
        return retVal.getAny().size() == 0 ? null : retVal;
    }

    @Override
    public boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        Log.d("SimpleEvaluator", "isExpressionPermittedInNamespace called");
        throw new UnsupportedOperationException("Topic namespace not supported yet!");
    }

    @Override
    public List<QName> evaluateTopicExpressionToQName(TopicExpressionType topicExpressionType, NamespaceContext context)
            throws UnsupportedOperationException, InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault,
            TopicExpressionDialectUnknownFault {

        Log.d("SimpleEvaluator", "evaluateTopicExpressionToQName called");

        if (!dialectURI.equals(topicExpressionType.getDialect()))
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "Simple evaluator can evaluate Simple dialect!");

        String expression = TopicUtils.extractExpression(topicExpressionType);
        // The topic expression should now be trimmed. Check for whitespace occurrence
        for (int i = 0; i < expression.length(); i++) {
            if (Character.isWhitespace(expression.charAt(i)))
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not in SimpleExpressionDialect; " +
                        "it contained whitespace where disallowed");
        }
        // Split expression in prefix and local part
        // If the expression started with "/", remove the first letter or throw an exception
        if (expression.length() > 0 && expression.charAt(0) == '/') {
            if (TopicValidator.isSlashAsSimpleAndConcreteDialectStartAccepted()) {
                Log.w("SimpleEvaluator[Topic]", "A simple expression started with \"/\" which was omitted.");
                expression = expression.substring(1);
            } else {
                Log.w("SimpleEvaluator[Topic]", "A simple expression started with \"/\" and was rejected.");
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not in SimpleExpressionDialect." +
                        " It started with an illegal character ('/')");
            }
        }

        String[] splitExpression = expression.split(":");
        if (splitExpression.length == 0 || splitExpression.length > 2) {
            // Check if local part contains "/", which si disallowed
            if (splitExpression[1].split("/").length != 1)
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not a SimpleExpressionDialect; " +
                        "multiple QName prefixes detected.");
        }
        if (splitExpression.length == 2) {
            // Check if local part contains "/", which si disallowed
            if (splitExpression[1].contains("/"))
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not a SimpleExpressionDialect; " +
                        "local part wsa a path expression.");
            String ns = context.getNamespaceURI(splitExpression[0]);
            if (ns == null) {
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not a SimpleExpressionDialect; " +
                        "namespace prefix not recognized");
            }
            List<QName> list = new ArrayList<>();
            list.add(new QName(ns, splitExpression[1], splitExpression[0]));
            return list;
        } else {
            if (splitExpression[0].contains("/"))
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not a SimpleExpressionDialect; " +
                        "local part was a path expression.");
            List<QName> list = new ArrayList<>();
            list.add(new QName(splitExpression[0]));
            return list;
        }
    }

    @Override
    public boolean isLegalExpression(TopicExpressionType topicExpressionType, NamespaceContext namespaceContext) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        Log.d("SimpleEvaluator", "isLegalExpression called");

        if (!dialectURI.equals(topicExpressionType.getDialect())) {
            Log.w("SimpleEvaluator[Topic]", "Was asked to check a non-simple expression");
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "Simple evaluator can evaluate simple dialect!");
        }

        Log.d("SimpleEvaluator[Topic]", "Checking for legality in TopicExpression");

        try {
            evaluateTopicExpressionToQName(topicExpressionType, namespaceContext);
            return true;
        } catch (MultipleTopicsSpecifiedFault multipleTopicsSpecifiedFault) {
            multipleTopicsSpecifiedFault.printStackTrace();
            Log.e("SimpleEvaluator[Topic]", "A simple expression was determined to specify multiple topics, " +
                    "which is impossible");
            return false;
        }
    }
}
