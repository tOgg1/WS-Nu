package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 13.03.14.
 */
public class TopicUtils {

    public static final String WS_TOPIC_NAMESPACE = "http://docs.oasis-open.org/wsn/t-1";

    /**
     * Should never be instantiated.
     */
    private TopicUtils() {}

    public static List<QName> topicSetToQNameList(TopicSetType set) {
        List<QName> list = new ArrayList<>();
        for (Object o: set.getAny()) {
            if (o instanceof Node) {
                Node node = (Node)o;
                list.add(topicNodeToQName(node));
            }
        }
        return list;
    }

    public static TopicSetType qNameListToTopicSet(List<QName> names) {
        // TODO
        return null;
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

    public static QName topicNodeToQName(Node topicNode) {
        // TODO Can the node be anything but a topic at this point?
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

    public static void addTopicToTopicSet(Node topic, TopicSetType topicSet) {
        // TODO Is it necessary to enforce that this is actually a topic?
        if (!isTopic(topic))
            throw new IllegalArgumentException("Tried to add a non-topic to a topic set!");
        // TODO
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
        Node topicAttr = nodeMap.getNamedItem("topic");
        if (topicAttr == null)
            return false;
        return topicAttr.getTextContent().equalsIgnoreCase("true");
    }
}
