package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.w3c.dom.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Inge on 13.03.14.
 */
public class TopicUtils {

    public static final String WS_TOPIC_NAMESPACE = "http://docs.oasis-open.org/wsn/t-1";

    /**
     * Should never be instantiated.
     */
    private TopicUtils() {}

    public static List<QName> topicSetToQNameList(TopicSetType set, boolean recursive) {
        if (recursive)
            return topicSetToQNameListRecursive(set);
        return topicSetToQNameListNonRecursive(set);
    }

    private static List<QName> topicSetToQNameListNonRecursive(TopicSetType set) {
        List<QName> list = new ArrayList<>();
        for (Object o: set.getAny()) {
            if (o instanceof Node) {
                Node node = (Node)o;
                list.add(topicNodeToQName(node));
            }
        }
        return list;
    }

    private static List<QName> topicSetToQNameListRecursive(TopicSetType set) {
        List<QName> list = new ArrayList<>();
        Stack<Node> nodeStack = new Stack<>();
        // Push all nodes to stack we are exploring from.
        for (Object o: set.getAny()) {
            if (o instanceof Node) {
                nodeStack.push((Node) o);
            }
        }
        while(!nodeStack.empty()) {
            // Pop a node from stack, if it is a topic node, add it to the topic list
            Node node = nodeStack.pop();
            QName nodeName = topicNodeToQName(node);
            if (nodeName != null) {
                list.add(nodeName);
            }
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

    public static TopicSetType qNameListToTopicSet(List<QName> names) {
        TopicSetType topicSet = new TopicSetType();
        for (QName qName: names) {
            addTopicToTopicSet(qName, topicSet);
        }
        return topicSet;
    }
/* TODO not prioritized
    public static List<TopicType> topicSetToTopicTypeList(TopicSetType set) {
        List<TopicType> list = new ArrayList<>();
        for (Object o: set.getAny()) {
            if (o instanceof Node) {
                Node node = (Node)o;
                // TODO remove:
                System.out.println("\n\n" + topicToQName(node) + "\n");
                System.out.println("PARENT:");
                printNode(node.getParentNode());

                node.normalize();
                list.add(nodeToTopicType(node));
            }
        }
        return list;
    }

    public static TopicSetType topicTypeListToTopicSet(List<TopicType> list) {
        // TODO
        return null;
    }

    private static TopicType nodeToTopicType(Node node) {
        // TODO This will be written recursively here. Since topic tree may be arbitrary deep, this implementation is prone to stack overflow issues
        TopicType topicType = new TopicType();
        // TODO Fill in with more data
        if (node.hasChildNodes()) {
            Node child = node.getFirstChild();
            while (child != null) {
                topicType.getTopic().add(nodeToTopicType(child));
                child = child.getNextSibling();
            }
        }
        // TODO remove
        printNode(node);
        return topicType;
    }
*/
    private static void printNode(Node node) {
        // TODO remove, debugging code
        System.out.println("\n\nNode:\t"+node.getLocalName());
        System.out.println("\tHas parent:\t" + (node.getParentNode() != null));
        System.out.println("\tBaseURI:\t" + node.getBaseURI());
        System.out.println("\tText content:\t" + node.getTextContent());
        System.out.println("\tNamespace URI:\t" + node.getNamespaceURI());
        System.out.println("\tNode name:\t" + node.getNodeName());
        System.out.println("\tValue:\t" + node.getNodeValue());
    }

    /**
     * Converts a topicNode to a QName.
     * @param topicNode the node to convert
     * @return qname of topic or null if node is not a topic.
     */
    public static QName topicNodeToQName(Node topicNode) {
        if (!isTopic(topicNode))
            return null;
        // Check for topic tag. If not present, throw an IllegalArgumentException
        String namespace = topicNode.getNamespaceURI();
        String localName = topicNode.getLocalName();
        String prefix = topicNode.getPrefix();
        topicNode = topicNode.getParentNode();
        while(namespace == null && topicNode != null) {
            String local = topicNode.getLocalName();
            if (local != null)
                localName = local +"/"+ localName;
            prefix = topicNode.getPrefix();
            namespace = topicNode.getNamespaceURI();
            topicNode = topicNode.getParentNode();
        }
        return new QName(namespace, localName, prefix);
    }

    public static void addTopicToTopicSet(QName topic, TopicSetType topicSet) {
        Node topicNode = qNameToTopicNode(topic);
        addTopicToTopicSet(topicNode, topicSet);
    }

    /**
     * Merges the tree defined from the parent of the topic that has common path as a child in topicSet
     * @param topic The topic that shall be added from its common root as is in or is added to topicSet
     * @param topicSet The TopicSetType to merge topic into
     */
    public static void addTopicToTopicSet(Node topic, TopicSetType topicSet) {
        if (!isTopic(topic))
            throw new IllegalArgumentException("Tried to add a non-topic to a topic set!");
        // A stack that will contain the parent nodes of topic from top to bottom.
        Stack<Node> topicStack = new Stack<>();
        // Add the topic itself to the stack, and go through all its parents until a namespace is defined.
        topicStack.push(topic);
        String namespace = topic.getNamespaceURI();
        Node current = topic;
        while (namespace == null && current != null) {
            current = current.getParentNode();
            namespace = current.getNamespaceURI();
            if (current != null)
                topicStack.push(current);
        }

        // Find element to merge from, if any
        Node mergeFromNode = null;
        for (Object o: topicSet.getAny()) {
            if (o instanceof  Node) {
                Node setNode = (Node) o;
                String setNS = setNode.getNamespaceURI();
                if (setNS == null && namespace == null) {
                    mergeFromNode = setNode;
                    break;
                }
                if (setNS != null && setNS.equals(namespace)) {
                    mergeFromNode = setNode;
                    break;
                }
            }
        }

        // If we did not find any to merge from, we can just add it to the any list in the topicSet
        if (mergeFromNode == null) {
            topicSet.getAny().add(topicStack.pop());
            return;
        }
        // If not, we find the first element that does not already exist in topicSet and merge from there
        current = topicStack.pop();
        // Ensure there still are elements to merge from, and that we shall continue down the set tree
        boolean foundNode = false;
        while ((!topicStack.empty()) && current.getLocalName().equals(mergeFromNode.getLocalName())) {
            // pop next node
            current = topicStack.pop();
            // Find child of topicSetMergeNode that fits current node
            Node correctChild = findElementWithLocalName(mergeFromNode, current.getLocalName());
            if (correctChild == null) {
                foundNode = true;
                break;
            }
            mergeFromNode = correctChild;
        }
        if (foundNode) {
            // The mergeFromNode is now where we shall inject current
            mergeFromNode.appendChild(current);
        } else {
            // We stopped looking for element because stack went dry. We must therefore ensure that mergeNode is topic
            makeTopicNode(mergeFromNode);
        }
    }

    private static Node findElementWithLocalName(Node root, String localName) {
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            // see if child is element, and if so, has local name equal to string given
            Node child = nodeList.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(localName))
                return child;
        }
        // Did not find child, return null
        return null;
    }

    public static Node qNameToTopicNode(QName name) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document owner = factory.newDocumentBuilder().newDocument();
            String localName = name.getLocalPart();
            String[] paths = localName.split("/");
            if (paths == null || paths.length == 0)
                throw new IllegalArgumentException("QName was ill formed: could not create topic node!");
            Node root = owner.createElementNS(name.getNamespaceURI(), paths[0]);
            owner.appendChild(root);
            Node finalNode = root;
            for (int i = 1; i < paths.length; i++) {
                Node newNode = owner.createElement(paths[i]);
                finalNode.appendChild(newNode);
                finalNode = newNode;
            }
            // Add the topic attribute to the final node
            makeTopicNode(finalNode);
            return finalNode;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void makeTopicNode(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE)
            throw new IllegalArgumentException("Tried to make a non-element node topic!");
        Document owner = node.getOwnerDocument();
        Attr topicAttr = owner.createAttributeNS(WS_TOPIC_NAMESPACE, "topic");
        topicAttr.setValue("true");
        node.appendChild(topicAttr);
    }

    public static boolean isTopic(Node node) {
        NamedNodeMap nodeMap = node.getAttributes();
        if (nodeMap == null)
            return false;
        Node topicAttr = nodeMap.getNamedItemNS(WS_TOPIC_NAMESPACE, "topic");
        if (topicAttr == null)
            return false;
        return topicAttr.getTextContent().equalsIgnoreCase("true");
    }
}
