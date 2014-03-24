package org.ntnunotif.wsnu.base.topics;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.oasis_open.docs.wsn.b_2.InvalidTopicExpressionFaultType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsrf.bf_2.BaseFaultType;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

/**
 * Created by Inge on 13.03.14.
 */
public class TopicUtils {

    public static final String WS_TOPIC_NAMESPACE = "http://docs.oasis-open.org/wsn/t-1";

    /**
     * Should never be instantiated.
     */
    private TopicUtils() {
    }

    public static List<List<QName>> topicSetToQNameList(TopicSetType set, boolean recursive) {
        if (recursive)
            return topicSetToQNameListRecursive(set);
        return topicSetToQNameListNonRecursive(set);
    }

    private static List<List<QName>> topicSetToQNameListNonRecursive(TopicSetType set) {
        List<List<QName>> list = new ArrayList<>();
        for (Object o : set.getAny()) {
            if (o instanceof Node) {
                Node node = (Node) o;
                List<QName> name = topicNodeToQNameList(node);
                if (name != null)
                    list.add(name);
            }
        }
        return list;
    }

    private static List<List<QName>> topicSetToQNameListRecursive(TopicSetType set) {
        List<List<QName>> list = new ArrayList<>();
        Stack<Node> nodeStack = new Stack<>();
        // Push all nodes to stack we are exploring from.
        for (Object o : set.getAny()) {
            if (o instanceof Node) {
                nodeStack.push((Node) o);
            }
        }
        while (!nodeStack.empty()) {
            // Pop a node from stack, if it is a topic node, add it to the topic list
            Node node = nodeStack.pop();
            List<QName> nodeName = topicNodeToQNameList(node);
            if (nodeName != null)
                list.add(nodeName);

            // Get all its element children and add them to the stack
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE)
                    nodeStack.push(child);
            }
        }
        return list;
    }

    public static TopicSetType qNameListListToTopicSet(List<List<QName>> names) {
        TopicSetType topicSet = new TopicSetType();
        for (List<QName> qNameList : names) {
            addTopicToTopicSet(qNameList, topicSet);
        }
        return topicSet;
    }

    /**
     * Converts a topicNode to a QName List.
     *
     * @param topicNode the node to convert
     * @return List of QNames representing topic or null if node is not a topic.
     */
    public static List<QName> topicNodeToQNameList(Node topicNode) {
        if (!isTopic(topicNode))
            return null;
        List<QName> qNames = new ArrayList<>();
        Node current = topicNode;
        // TODO check up on if assumption that if grandparent is null, we are at root topic
        // Create a stack holding all the topics we shall add to list of QNames from root to leaf
        Stack<Node> nodeStack = new Stack<>();

        // build stack
        while (current.getParentNode() != null) {
            nodeStack.push(current);
            current = current.getParentNode();
        }

        // Go through stack, creating QNames as we go and add them to list
        while (!nodeStack.empty()) {
            current = nodeStack.pop();
            String ns = current.getNamespaceURI();
            String name = current.getLocalName() == null ? current.getNodeName() : current.getLocalName();
            if (ns == null || ns.equals(XMLConstants.NULL_NS_URI)) {
                qNames.add(new QName(name));
            } else {
                if (current.getPrefix() == null)
                    qNames.add(new QName(ns, name));
                else
                    qNames.add(new QName(ns, name, current.getPrefix()));
            }
        }

        return qNames;
    }

    public static void addTopicToTopicSet(List<QName> topic, TopicSetType topicSet) {
        Node topicNode = qNameListToTopicNode(topic);
        addTopicToTopicSet(topicNode, topicSet);
    }

    /**
     * Merges the tree defined from the parent of the topic that has common path as a child in topicSet
     *
     * @param topic    The topic that shall be added from its common root as is in or is added to topicSet
     * @param topicSet The TopicSetType to merge topic into
     */
    public static void addTopicToTopicSet(Node topic, TopicSetType topicSet) {
        if (!isTopic(topic))
            throw new IllegalArgumentException("Tried to add a non-topic to a topic set!");
        // A stack that will contain the parent nodes of topic from top to bottom.
        Stack<Node> topicStack = new Stack<>();
        // TODO check this assumption:
        // Add the topic itself to the stack, and go through all its parents until grandparent is not defined.
        topicStack.push(topic);
        Node current = topic.getParentNode();
        while (current.getParentNode() != null) {
            topicStack.push(current);
            current = current.getParentNode();
        }

        // Pop first element from stack, so we can see what we should merge from
        current = topicStack.pop();

        // Find root element to merge from, if any. Sort by both local names and namespaces
        Node mergeFromNode = null;
        String topNS = current.getNamespaceURI();
        String topName = current.getLocalName() == null ? current.getNodeName() : current.getLocalName();
        String setNS;
        String setName;
        for (Object o : topicSet.getAny()) {
            if (o instanceof Node) {
                Node setNode = (Node) o;
                setNS = setNode.getNamespaceURI();
                setName = setNode.getLocalName() == null ? setNode.getNodeName() : setNode.getLocalName();
                if ((setNS == null || setNS.equals(XMLConstants.NULL_NS_URI)) && (topNS == null || topNS.equals(XMLConstants.NULL_NS_URI))) {
                    if (setName.equals(topName)) {
                        mergeFromNode = setNode;
                        break;
                    }
                }
                if (setNS != null && setNS.equals(topNS)) {
                    if (setName.equals(topName)) {
                        mergeFromNode = setNode;
                        break;
                    }
                }
            }
        }

        // If we did not find any to merge from, we can just add it to the any list in the topicSet
        if (mergeFromNode == null) {
            topicSet.getAny().add(current);
            return;
        }

        // If not, we find the first element that does not already exist in topicSet and merge from there
        // Ensure there still are elements to merge from, and that we shall continue down the set tree
        boolean foundNode = false;
        setNS = mergeFromNode.getNamespaceURI();
        setName = mergeFromNode.getLocalName() == null ? mergeFromNode.getNodeName() : mergeFromNode.getLocalName();
        // Helper to make namespace comparison easier
        boolean bothNSisNull = (topNS == null || topNS.equals(XMLConstants.NULL_NS_URI)) &&
                (setNS == null || setNS.equals(XMLConstants.NULL_NS_URI));
        while ((!topicStack.empty()) && topName.equals(setName) && (bothNSisNull || (topNS != null && topNS.equals(setNS)))) {
            // pop next node
            current = topicStack.pop();
            topName = current.getLocalName() == null ? current.getNodeName() : current.getLocalName();
            topNS = current.getNamespaceURI();
            // Find child of topicSetMergeNode that fits current node
            Node correctChild = findElementWithNameAndNamespace(mergeFromNode, topName, topNS);
            // If there were no children, the merge from node is the one we should merge from.
            if (correctChild == null) {
                foundNode = true;
                break;
            }
            mergeFromNode = correctChild;
            setName = mergeFromNode.getLocalName() == null ? mergeFromNode.getNodeName() : mergeFromNode.getLocalName();
            setNS = mergeFromNode.getNamespaceURI();
            bothNSisNull = (topNS == null || topNS.equals(XMLConstants.NULL_NS_URI)) &&
                    (setNS == null || setNS.equals(XMLConstants.NULL_NS_URI));
        }
        if (foundNode) {
            // The mergeFromNode is now where we shall inject current
            Node importedNode = mergeFromNode.getOwnerDocument().importNode(current, true);
            mergeFromNode.appendChild(importedNode);
        } else {
            // We stopped looking for element because stack went dry. We must therefore ensure that mergeNode is topic
            makeTopicNode(mergeFromNode);
        }
    }

    public static Node findElementWithNameAndNamespace(Node root, String name, String namespace) {
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            // see if child is element, and if so, has local name equal to string given
            Node child = nodeList.item(i);
            String n = child.getLocalName() == null ? child.getNodeName() : child.getLocalName();
            String ns = child.getNamespaceURI();
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (namespace == null || namespace.equals(XMLConstants.NULL_NS_URI)) {
                    if (n != null && (ns == null || ns.equals(XMLConstants.NULL_NS_URI)) && n.equals(name))
                        return child;
                } else {
                    if (n != null && n.equals(name) && ns.equals(namespace))
                        return child;
                }
            }

        }
        // Did not find child, return null
        return null;
    }

    public static Node qNameListToTopicNode(List<QName> nameList) {
        if (nameList == null || nameList.size() == 0)
            return null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document owner = factory.newDocumentBuilder().newDocument();

            Node node = null;
            for (QName qName : nameList) {
                Node oldNode = node;
                String lName = qName.getLocalPart();
                String ns = qName.getNamespaceURI();
                if (ns == null || ns.equals(XMLConstants.NULL_NS_URI)) {
                    node = owner.createElement(lName);
                } else {
                    node = owner.createElementNS(ns, lName);
                }
                if (oldNode == null)
                    owner.appendChild(node);
                else
                    oldNode.appendChild(node);
            }

            if (node != null)
                makeTopicNode(node);
            return node;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void makeTopicNode(Node node) {
        if (node == null)
            return;
        if (node.getNodeType() != Node.ELEMENT_NODE)
            throw new IllegalArgumentException("Tried to make a non-element node topic!");
        Element element = (Element) node;
        Document owner = element.getOwnerDocument();
        Attr topicAttr = owner.createAttributeNS(WS_TOPIC_NAMESPACE, "topic");
        topicAttr.setValue("true");
        element.setAttributeNodeNS(topicAttr);
    }

    public static boolean isTopic(Node node) {
        if (node == null)
            return false;
        NamedNodeMap nodeMap = node.getAttributes();
        if (nodeMap == null)
            return false;
        Node topicAttr = nodeMap.getNamedItemNS(WS_TOPIC_NAMESPACE, "topic");
        if (topicAttr == null)
            return false;
        return topicAttr.getTextContent().equalsIgnoreCase("true");
    }

    public static String extractExpression(TopicExpressionType topicExpressionType) throws InvalidTopicExpressionFault{
        String expression = null;
        for (Object o : topicExpressionType.getContent()) {
            if (o instanceof String) {
                if (expression != null) {
                    InvalidTopicExpressionFaultType faultType = new InvalidTopicExpressionFaultType();
                    faultType.setTimestamp(new XMLGregorianCalendarImpl(
                            new GregorianCalendar(TimeZone.getTimeZone("UTC"))));
                    BaseFaultType.Description description = new BaseFaultType.Description();
                    description.setLang("en");
                    description.setValue("The given content of the expression was not a simple expression!");
                    faultType.getDescription().add(description);
                    throw new InvalidTopicExpressionFault(description.getValue(), faultType);
                }
                expression = (String) o;
            }
        }
        if (expression == null) {
            InvalidTopicExpressionFaultType faultType = new InvalidTopicExpressionFaultType();
            faultType.setTimestamp(new XMLGregorianCalendarImpl(new GregorianCalendar(TimeZone.getTimeZone("UTC"))));
            BaseFaultType.Description description = new BaseFaultType.Description();
            description.setLang("en");
            description.setValue("No expression was given, and thus can not be evaluated!");
            faultType.getDescription().add(description);
            throw new InvalidTopicExpressionFault(description.getValue(), faultType);
        }
        return expression.trim();
    }

    public static void throwInvalidTopicExpressionFault(String lang, String desc) throws InvalidTopicExpressionFault {
        InvalidTopicExpressionFaultType faultType = new InvalidTopicExpressionFaultType();
        faultType.setTimestamp(new XMLGregorianCalendarImpl(new GregorianCalendar(TimeZone.getTimeZone("UTC"))));
        BaseFaultType.Description description = new BaseFaultType.Description();
        description.setLang(lang);
        description.setValue(desc);
        faultType.getDescription().add(description);
        throw new InvalidTopicExpressionFault(desc, faultType);
    }
}
