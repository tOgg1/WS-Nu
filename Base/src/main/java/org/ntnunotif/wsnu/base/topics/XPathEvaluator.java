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
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.util.List;
import java.util.Stack;

/**
 * A <code>XPathEvaluator</code> is an evaluator that support the xpath dialect, defined by OASIS WS-Topics 1.3
 */
public class XPathEvaluator implements TopicExpressionEvaluatorInterface {

    /**
     * The dialect this evaluator supports
     */
    public static final String dialectURI = "http://www.w3.org/TR/1999/REC-xpath-19991116";

    @Override
    public String getDialectURIAsString() {
        return dialectURI;
    }

    @Override
    public boolean evaluateTopicWithExpression(TopicExpressionType topicExpressionType, TopicType topicType)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        Log.d("XPathEvaluator", "evaluateTopicWithExpression called");
        throw new UnsupportedOperationException("Namespace evaluation is still not implemented");
    }

    @Override
    public TopicSetType getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType,
                                        NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        Log.d("XPathEvaluator", "getIntersection called");

        if (!dialectURI.equals(topicExpressionType.getDialect()))
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "XPath evaluator can evaluate XPath dialect!");

        // Find expression string
        String expression = TopicUtils.extractExpression(topicExpressionType);

        return getXpathIntersection(expression, topicSetType, namespaceContext);
    }

    @Override
    public boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        Log.d("XPathEvaluator", "isExpressionPermittedInNamespace called");
        throw new UnsupportedOperationException("Permittance in namespace is still not implemented");
    }

    @Override
    public List<QName> evaluateTopicExpressionToQName(TopicExpressionType topicExpressionType, NamespaceContext context)
            throws UnsupportedOperationException, InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault {
        Log.d("XPathEvaluator", "evaluateTopicExpressionToQName called");
        String expression = TopicUtils.extractExpression(topicExpressionType);

        return FullEvaluator.evaluateFullTopicExpressionToQNameList(expression, context);
    }

    @Override
    public boolean isLegalExpression(TopicExpressionType topicExpressionType, NamespaceContext namespaceContext) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        Log.d("XPathEvaluator", "isLegalExpression called");

        if (!dialectURI.equals(topicExpressionType.getDialect())) {
            Log.w("XPathEvaluator[Topic]", "Was asked to check a non-XPath expression");
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "XPath evaluator can evaluate XPath dialect!");
        }
        // Extract the expression
        String expression = TopicUtils.extractExpression(topicExpressionType);
        // Build XPath environment
        XPath xPath = XPathFactory.newInstance().newXPath();
        // Set up correct context
        xPath.setNamespaceContext(namespaceContext);
        // Try to compile the expression

        try {
            xPath.compile(expression);
            Log.d("XPathEvaluator[Topic]", "checked and accepted legal XPath expression: " + expression);
            return true;
        } catch (XPathExpressionException e) {
            Log.w("XPathEvaluator[Topic]", "Was asked to check a malformed XPath expression");
            TopicUtils.throwInvalidTopicExpressionFault("en", "Topic expression did not follow correct XPath syntax");
            return false;
        }
    }

    /**
     * Gets the intersection between an expression interpreted as a XPath expression, and a TopicSet
     *
     * @param expression the expression in xpath dialect
     * @param setType    the set to intersect with
     * @param context    the context the expression stood in
     * @return the intersection between the set and the expression
     * @throws InvalidTopicExpressionFault if the expression is malformed in any way
     */
    public static TopicSetType getXpathIntersection(String expression, TopicSetType setType, NamespaceContext context)
            throws InvalidTopicExpressionFault {

        Log.d("XPathEvaluator", "getXpathIntersection called");

        // Build XPath environment
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        // Set up correct context
        xPath.setNamespaceContext(context);
        XPathExpression xPathExpression;


        try {
            xPathExpression = xPath.compile(expression);
        } catch (XPathExpressionException e) {
            TopicUtils.throwInvalidTopicExpressionFault("en", "Topic expression did not follow correct XPath syntax");
            // Dead code:
            return null;
        }

        // For every object in topicSet, try to evaluate it against expression and store result.
        int returnCount = 0;
        TopicSetType returnSet = new TopicSetType();
        for (Object o : setType.getAny()) {

            try {
                // Evaluate expression
                NodeList nodeList = (NodeList) xPathExpression.evaluate(o, XPathConstants.NODESET);

                // If result contained topics, add them to return topic set.
                returnCount += nodeList.getLength();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);

                    // Ensure that it is actual topics we have selected
                    if (TopicUtils.isTopic(node)) {

                        // All nodes need to be imported to new tree, to ensure we do not change other results
                        // Go to root node and add it
                        Stack<Node> nodeStack = new Stack<>();
                        nodeStack.push(node);

                        while (node != null && node.getParentNode() != null && node.getParentNode().getParentNode() != null) {
                            node = node.getParentNode();
                            nodeStack.push(node);
                        }

                        try {
                            // Try to build the topic list from root to end
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            factory.setNamespaceAware(true);
                            Document owner = factory.newDocumentBuilder().newDocument();
                            Element firstChild = owner.createElement("SetRoot");

                            node = owner.importNode(nodeStack.pop(), false);
                            firstChild.appendChild(node);

                            //owner.appendChild(node);
                            returnSet.getAny().add(node);

                            while (!nodeStack.empty()) {
                                // Make sure the previous node is not marked as topic
                                TopicUtils.forceNonTopicNode(node);
                                Node current = owner.importNode(nodeStack.pop(), false);
                                node.appendChild(current);
                                node = current;
                            }
                        } catch (ParserConfigurationException e) {
                            Log.e("XPathEvaluator", "Tried to build a topic set, but failed. " + e.getMessage());
                        }
                    }
                }
            } catch (XPathExpressionException e) {
                TopicUtils.throwInvalidTopicExpressionFault("en", "Some part of expression failed evaluation. " +
                        "This can not be a legal XPath expression");
            }
        }
        if (returnCount == 0)
            return null;
        return returnSet;
    }
}
