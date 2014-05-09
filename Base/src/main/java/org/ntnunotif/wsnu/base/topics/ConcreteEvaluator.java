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
 * A <code>ConcreteEvaluator</code> is an evaluator that support the concrete dialect, defined by OASIS WS-Topics 1.3
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
        Log.d("ConcreteEvaluator", "evaluateTopicWithExpression called");
        throw new UnsupportedOperationException("Topic namespace not supported yet!");
    }

    @Override
    public TopicSetType getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType,
                                        NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        Log.d("ConcreteEvaluator", "getIntersection called");

        if (!dialectURI.equals(topicExpressionType.getDialect()))
            TopicUtils.throwTopicExpressionDialectUnknownFault("en",
                    "Concrete evaluator can evaluate Concrete dialect!");

        List<QName> topic;
        try {
            topic = evaluateTopicExpressionToQName(topicExpressionType, namespaceContext);
        } catch (MultipleTopicsSpecifiedFault multipleTopicsSpecifiedFault) {
            // This is impossible in concrete dialect
            multipleTopicsSpecifiedFault.printStackTrace();
            return null;
        }
        for (Object o : topicSetType.getAny()) {
            int topicNumber = 0;
            // get root name, and post increment topicNumber
            QName curName = topic.get(topicNumber++);

            // boolean used for checking if this roots namespace is null.
            boolean curNamespaceNull = curName.getNamespaceURI() == null ||
                    curName.getNamespaceURI().equals(XMLConstants.NULL_NS_URI);

            // An element that can be a topic is a node. Only proceed if it is
            if (o instanceof Node) {
                Node node = (Node) o;
                String nodeNS = node.getNamespaceURI();
                String nodeName = node.getLocalName() == null ? node.getNodeName() : node.getLocalName();

                // Ensure last letter in namespace is not /
                if (nodeNS != null && nodeNS.length() != 0 && (nodeNS.charAt(nodeNS.length() - 1) == '/' || nodeNS.charAt(nodeNS.length() - 1) == ':')) {
                    nodeNS = nodeNS.substring(0, nodeNS.length() - 1);
                }

                // Defining curNS as current namespace, and removes the last character if it is /
                String curNS = curName.getNamespaceURI();
                if (curNS != null && curNS.length() != 0 && (curNS.charAt(curNS.length() - 1) == '/' || curNS.charAt(curNS.length() - 1) == ':')) {
                    curNS = curNS.substring(0, curNS.length() - 1);
                }

                // Both namespaces must either be null or equal
                if (((nodeNS == null || nodeNS.equals(XMLConstants.NULL_NS_URI)) && curNamespaceNull) ||
                        (nodeNS != null && nodeNS.equals(curNS))) {

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
                            try {
                                TopicSetType returnSet = new TopicSetType();

                                // The returned set should only contain a single topic node with only non-topic parents
                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                factory.setNamespaceAware(true);
                                Document owner = factory.newDocumentBuilder().newDocument();
                                Element firstChild = owner.createElement("SetRoot");
                                Node previousElement = owner.importNode(node, false);

                                while (node != null && node.getParentNode() != null && node.getParentNode().getParentNode() != null) {
                                    node = node.getParentNode();
                                    Node currentElement = owner.importNode(node, false);
                                    TopicUtils.forceNonTopicNode(currentElement);
                                    currentElement.appendChild(previousElement);
                                    previousElement = currentElement;
                                }

                                firstChild.appendChild(previousElement);

                                returnSet.getAny().add(previousElement);
                                return returnSet;
                            } catch (Exception e) {
                                Log.e("ConcreteEvaluator", "intersection with concrete dialect failed " + e.getMessage());
                            }
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
        Log.d("ConcreteEvaluator", "isExpressionPermittedInNamespace called");
        throw new UnsupportedOperationException("Topic namespace not supported yet!");
    }

    @Override
    public List<QName> evaluateTopicExpressionToQName(TopicExpressionType topicExpressionType, NamespaceContext context)
            throws UnsupportedOperationException, InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault,
            TopicExpressionDialectUnknownFault {

        Log.d("ConcreteEvaluator", "evaluateTopicExpressionToQName called");

        if (!dialectURI.equals(topicExpressionType.getDialect()))
            TopicUtils.throwTopicExpressionDialectUnknownFault("en",
                    "Concrete evaluator can evaluate Concrete dialect!");

        String expression = TopicUtils.extractExpression(topicExpressionType);
        for (int i = 0; i < expression.length(); i++) {
            if (Character.isWhitespace(expression.charAt(i)))
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not a " +
                        "ConcreteExpressionDialect; it contained whitespace where disallowed");
        }
        // Create the list containing the QNames we wish to return
        List<QName> retVal = new ArrayList<>();
        // Split expression in its individual path parts

        // If the expression started with "/", remove the first letter, or throw an exception
        if (expression.length() > 0 && expression.charAt(0) == '/') {
            if (TopicValidator.isSlashAsSimpleAndConcreteDialectStartAccepted()) {
                Log.w("ConcreteEvaluator[Topic]", "A concrete expression started with \"/\" which was omitted.");
                expression = expression.substring(1);
            } else {
                Log.w("ConcreteEvaluator[Topic]", "A concrete expression started with \"/\" and was rejected.");
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not in " +
                        "ConcreteExpressionDialect. It started with an illegal character ('/')");
            }
        }

        String[] pathed = expression.split("/");
        for (String str : pathed) {
            String[] name = str.split(":");
            if (name.length == 0 || name.length > 2)
                TopicUtils.throwInvalidTopicExpressionFault("en", "The expression was not a " +
                        "ConcreteExpressionDialect; multiple prefixes in a QName was discovered");
            if (name.length == 1) {

                // The QName only has local part, check if it a legal NCName
                if (!TopicUtils.isNCName(name[0])) {
                    Log.w("ConcreteEvaluator[Topic]", "A concrete expression contained a character sequence that " +
                            "were not a NCName: " + name[0]);
                    TopicUtils.throwInvalidTopicExpressionFault("en", "The topic contained a illegal NCName " +
                            "identifier: " + name[0]);
                }

                retVal.add(new QName(name[0]));
            } else {
                // The QName is a prefix and a local part
                String prefix = name[0];
                String localName = name[1];

                // Check both for NCName legality
                if (!TopicUtils.isNCName(prefix) || !TopicUtils.isNCName(localName)) {
                    Log.w("ConcreteEvaluator[Topic]", "A concrete expression contained a character sequence that " +
                            "were not a NCName: " + prefix + ":" + localName);
                    TopicUtils.throwInvalidTopicExpressionFault("en", "The topic contained a illegal NCName " +
                            "identifier: " + prefix + ":" + localName);
                }

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

    @Override
    public boolean isLegalExpression(TopicExpressionType topicExpressionType, NamespaceContext namespaceContext) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        Log.d("ConcreteEvaluator", "isLegalExpression called");

        if (!dialectURI.equals(topicExpressionType.getDialect())) {
            Log.w("ConcreteEvaluator[Topic]", "Was asked to check a non-concrete expression");
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "Concrete evaluator can evaluate concrete dialect!");
        }

        Log.d("ConcreteEvaluator[Topic]", "Checking for legality in TopicExpression");

        try {
            evaluateTopicExpressionToQName(topicExpressionType, namespaceContext);
            return true;
        } catch (MultipleTopicsSpecifiedFault multipleTopicsSpecifiedFault) {
            multipleTopicsSpecifiedFault.printStackTrace();
            Log.e("ConcreteEvaluator[Topic]", "A concrete expression was determined to specify multiple topics, " +
                    "which is impossible");
            return false;
        }
    }
}
