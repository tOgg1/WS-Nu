package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 13.03.14.
 */
public class TopicUtils {
    /**
     * Should never be instantiated.
     */
    private TopicUtils() {}

    public static List<QName> topicSetToQNameList(TopicSetType set) {
        List<QName> list = new ArrayList<>();
        for (Object o: set.getAny()) {
            if (o instanceof Node) {
                Node node = (Node)o;
                list.add(topicToQName(node));
            }
        }
        return list;
    }

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

    private static void printNode(Node node) {
        System.out.println("\n\nNode:\t"+node.getLocalName());
        System.out.println("\tHas parent:\t" + (node.getParentNode() != null));
        System.out.println("\tBaseURI:\t" + node.getBaseURI());
        System.out.println("\tText content:\t" + node.getTextContent());
        System.out.println("\tNamespace URI:\t" + node.getNamespaceURI());
        System.out.println("\tNode name:\t" + node.getNodeName());
        System.out.println("\tValue:\t" + node.getNodeValue());
    }

    public static QName topicToQName(Node topicNode) {
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
}
