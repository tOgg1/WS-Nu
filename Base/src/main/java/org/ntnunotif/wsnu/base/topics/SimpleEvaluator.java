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

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 21.03.2014.
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
                String nodeName = node.getLocalName() == null ? node.getNodeName() : node.getLocalName();
                boolean bothNSisNull = topic.getNamespaceURI() == null ||
                        topic.getNamespaceURI().equals(XMLConstants.NULL_NS_URI);
                bothNSisNull = bothNSisNull && (nodeNS == null || nodeNS.equals(XMLConstants.NULL_NS_URI));
                if (bothNSisNull) {
                    if (topic.getLocalPart().equals(nodeName) && TopicUtils.isTopic(node)) {
                        retVal.getAny().add(node);
                        break;
                    }
                } else {
                    if (topic.getNamespaceURI() != null && topic.getNamespaceURI().equals(nodeNS) &&
                            topic.getLocalPart().equals(nodeName) && TopicUtils.isTopic(node)) {
                        retVal.getAny().add(node);
                        break;
                    }
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
