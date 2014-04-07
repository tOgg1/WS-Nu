package org.ntnunotif.wsnu.base.topics;

import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicNamespaceType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.List;

/**
 * Created by Inge on 10.03.14.
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
        throw new UnsupportedOperationException("Namespace evaluation is still not implemented");
    }

    @Override
    public TopicSetType getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType,
                                        NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        if (!dialectURI.equals(topicExpressionType.getDialect()))
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "XPath evaluator can evaluate XPath dialect!");

        // Find expression string
        String expression = TopicUtils.extractExpression(topicExpressionType);

        return getXpathIntersection(expression, topicSetType, namespaceContext);
    }

    @Override
    public boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        throw new UnsupportedOperationException("Permittance in namespace is still not implemented");
    }

    @Override
    public List<QName> evaluateTopicExpressionToQName(TopicExpressionType topicExpressionType, NamespaceContext context)
            throws UnsupportedOperationException, InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault {
        String expression = TopicUtils.extractExpression(topicExpressionType);

        return FullEvaluator.evaluateFullTopicExpressionToQNameList(expression, context);
    }

    @Override
    public boolean isLegalExpression(TopicExpressionType topicExpressionType, NamespaceContext namespaceContext) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
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

    public static TopicSetType getXpathIntersection(String expression, TopicSetType setType, NamespaceContext context)
            throws InvalidTopicExpressionFault {

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
        for (Object o: setType.getAny()) {

            try {
                // Evaluate expression
                NodeList nodeList = (NodeList) xPathExpression.evaluate(o, XPathConstants.NODESET);

                // If result contained topics, add them to return topic set.
                returnCount += nodeList.getLength();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);

                    // Ensure that it is actual topics we have selected
                    if (TopicUtils.isTopic(node))
                        returnSet.getAny().add(node);
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
