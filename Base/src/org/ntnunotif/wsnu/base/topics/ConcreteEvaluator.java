package org.ntnunotif.wsnu.base.topics;

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
public class ConcreteEvaluator implements TopicExpressionEvaluatorInterface {
    /**
     * The dialect this evaluator supports
     */
    public static final String dialectURI = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete";

    @Override
    public String getDialectURIAsString() {
        return dialectURI;
    }

    @Override
    public boolean evaluateTopicWithExpression(TopicExpressionType topicExpressionType, TopicType topicType)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        throw new UnsupportedOperationException("Topic namespace not supported yet!");
    }

    @Override
    public TopicSetType getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType,
                                        NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        List<QName> topic;
        try {
            topic = evaluateTopicExpressionToQName(topicExpressionType, namespaceContext);
        } catch (MultipleTopicsSpecifiedFault multipleTopicsSpecifiedFault) {
            // This is impossible in concrete dialect
            multipleTopicsSpecifiedFault.printStackTrace();
            return null;
        }
        for (Object o: topicSetType.getAny()) {
            int topicNumber = 0;
            // get root name, and post increment topicNumber
            QName curName = topic.get(topicNumber++);

            // boolean used for checking if this roots namespace is null.
            boolean curNamespaceNull = curName.getNamespaceURI() == null ||
                    curName.getNamespaceURI().equals(XMLConstants.NULL_NS_URI);

            // An element that can be a topic is a node. Only proceed if it is
            if (o instanceof Node) {
                Node node = (Node)o;
                String nodeNs = node.getNamespaceURI();
                String nodeName = node.getLocalName() == null ? node.getNodeName() : node.getLocalName();

                // Both namespaces must either be null or equal
                if (((nodeNs == null || nodeNs.equals(XMLConstants.NULL_NS_URI)) && curNamespaceNull) ||
                        (nodeNs != null && nodeNs.equals(curName.getNamespaceURI()))) {

                    // Both local names must be equal, and if they are, we have found the root node to check from
                    if (curName.getLocalPart().equals(nodeName)) {

                        // Check rest of nodes against topic name
                        while (topicNumber < topic.size()) {
                            curName = topic.get(topicNumber++);
                            node = TopicUtils.findElementWithNameAndNamespace(node, curName.getLocalPart(),
                                    curName.getNamespaceURI());

                            // If no node was found that fit the topic indicated by the expression, stop looping through
                            // this node tree.
                            if (node == null) {
                                // Ensure we do not get any faulty positives
                                topicNumber = -1;
                                break;
                            }
                        }

                        // if topicNumber now is equal to size, we have found the correct node. If this is a topic,
                        // return set with this node. Only one node can be found with concrete dialect
                        if (topicNumber == topic.size() && TopicUtils.isTopic(node)) {
                            TopicSetType returnSet = new TopicSetType();
                            returnSet.getAny().add(node);
                            return returnSet;
                        }
                    }
                }
            }
        }

        // If we get here, no nodes were found in set that is a topic and fit the concrete expression.
        return null;
    }

    @Override
    public boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        throw new UnsupportedOperationException("Topic namespace not supported yet!");
    }

    @Override
    public List<QName> evaluateTopicExpressionToQName(TopicExpressionType topicExpressionType, NamespaceContext context)
            throws UnsupportedOperationException, InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault {
        String expression = TopicUtils.extractExpression(topicExpressionType);
        for (int i = 0; i < expression.length(); i++) {
            if (Character.isWhitespace(expression.charAt(i)))
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not a " +
                        "ConcreteExpressionDialect; it contained whitespace where disallowed");
        }
        // Create the list containing the QNames we wish to return
        List<QName> retVal = new ArrayList<>();
        // Split expression in its individual path parts
        String[] pathed = expression.split("/");
        for (String str: pathed) {
            String[] name = str.split(":");
            if (name.length == 0 || name.length > 2)
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not a " +
                        "ConcreteExpressionDialect; multiple prefixes in a QName was discovered");
            if (name.length == 1) {
                // The QName only has local part
                retVal.add(new QName(name[0]));
            } else {
                // The QName is a prefix and a local part
                String prefix = name[0];
                String localName = name[1];
                String ns = context.getNamespaceURI(prefix);
                if (ns == null || ns.equals(XMLConstants.NULL_NS_URI)) {
                    TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not a " +
                            "ConcreteExpressionDialect; namespace prefix was not recognized");
                }
                retVal.add(new QName(ns, localName, prefix));
            }
        }
        return retVal;
    }
}
