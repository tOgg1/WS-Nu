package org.ntnunotif.wsnu.base.topics;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.oasis_open.docs.wsn.b_2.InvalidTopicExpressionFaultType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionDialectUnknownFaultType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsrf.bf_2.BaseFaultType;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

/**
 * Helper methods to use with topics. Should comply to OASIS standard wsn-ws_topics-1.3-spec-os.
 *
 * @author Inge Edward Halsaunet
 *         Created by Inge on 13.03.14.
 */
public class TopicUtils {

    /**
     * WS Topic Namespace, defined by OASIS
     */
    public static final String WS_TOPIC_NAMESPACE = "http://docs.oasis-open.org/wsn/t-1";

    /**
     * Should never be instantiated.
     */
    private TopicUtils() {
    }

    /**
     * Creates a list of list of QNames representing the topics in this {@link org.oasis_open.docs.wsn.t_1.TopicSetType}.
     * It will ensure that all Topics has the topic attribute topic before calculating the list. Has the possibility to
     * either calculate for only the roots within the set, or take out every topic it encounters.
     *
     * @param set       The set to translate
     * @param recursive if we should take all topics descending from the roots in the returned list.
     * @return The topics represented as lists of {@link javax.xml.namespace.QName}s.
     */
    public static List<List<QName>> topicSetToQNameList(TopicSetType set, boolean recursive) {
        if (recursive)
            return topicSetToQNameListRecursive(set);
        return topicSetToQNameListNonRecursive(set);
    }

    /**
     * Helper method for topicSetToQNameList. Does the non-recursive translation.
     *
     * @param set The set to translate
     * @return The topics represented as lists of {@link javax.xml.namespace.QName}s.
     * @see org.ntnunotif.wsnu.base.topics.TopicUtils#topicSetToQNameList(org.oasis_open.docs.wsn.t_1.TopicSetType, boolean)
     */
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

    /**
     * Helper method for topicSetToQNameList. Does the recursive translation.
     *
     * @param set The set to translate
     * @return The topics represented as lists of {@link javax.xml.namespace.QName}s.
     * @see org.ntnunotif.wsnu.base.topics.TopicUtils#topicSetToQNameList(org.oasis_open.docs.wsn.t_1.TopicSetType, boolean)
     */
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

    /**
     * Translates a list of list of {@link javax.xml.namespace.QName}s to
     * {@link org.oasis_open.docs.wsn.t_1.TopicSetType}. The leaf {@link org.w3c.dom.Node}s have the topic attribute set
     * to true.
     *
     * @param names the topics represented as list of {@link javax.xml.namespace.QName}s
     * @return the resulting {@link org.oasis_open.docs.wsn.t_1.TopicSetType}
     */
    public static TopicSetType qNameListListToTopicSet(List<List<QName>> names) {
        TopicSetType topicSet = new TopicSetType();
        for (List<QName> qNameList : names) {
            addTopicToTopicSet(qNameList, topicSet);
        }
        return topicSet;
    }

    /**
     * Converts a topicNode to a QName List. This method checks if the {@link org.w3c.dom.Node} has the topic attribute
     * set to <code>true</code>.
     *
     * @param topicNode the node to convert
     * @return List of QNames representing topic or <code>null</code> if node is not a topic.
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

    /**
     * Takes a topic represented as a list of {@link javax.xml.namespace.QName}s and adds it to the
     * {@link org.oasis_open.docs.wsn.t_1.TopicSetType}. It will find the correct branch in the topic tree, or it will
     * add a new branch to the topic tree.
     *
     * @param topic    The topic to add
     * @param topicSet The set to add the topic to
     */
    public static void addTopicToTopicSet(List<QName> topic, TopicSetType topicSet) {
        Node topicNode = qNameListToTopicNode(topic);
        addTopicToTopicSet(topicNode, topicSet);
    }

    /**
     * Takes a topic represented by a {@link org.w3c.dom.Node} and merges it to the set at the correct branch. It will
     * go to the root of the given topic to discover actual root of tree. It will throw an
     * {@link java.lang.IllegalArgumentException} if the given <code>Node</code> is not a topic. The method assumes the
     * node given has the root TopicSet, as defined by OASIS in wsn-ws_topics-1.3-spec-os.
     *
     * @param topic    The topic that shall be added from its common root as is in or is added to topicSet
     * @param topicSet The TopicSetType to merge topic into
     */
    public static void addTopicToTopicSet(Node topic, TopicSetType topicSet) {
        if (!isTopic(topic))
            throw new IllegalArgumentException("Tried to add a non-topic to a topic set!");
        // A stack that will contain the parent nodes of topic from top to bottom.
        Stack<Node> topicStack = new Stack<>();
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

                // If both namespaces is null, check the local name
                if ((setNS == null || setNS.equals(XMLConstants.NULL_NS_URI)) && (topNS == null || topNS.equals(XMLConstants.NULL_NS_URI))) {
                    if (setName.equals(topName)) {
                        mergeFromNode = setNode;
                        break;
                    }
                }

                // If namespaces are equal, check the local name
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

    /**
     * Looks into a {@link org.w3c.dom.Node}, and locates the child element with the given namespace and local name.
     * Returned <code>Node</code> is either a {@link Node#ELEMENT_NODE} or <code>null</code>.
     *
     * @param root      the <code>Node</code> to look into
     * @param name      the local name or node name of the element to look for
     * @param namespace the namespace the element should belong to, or <code>null</code> if not applicable
     * @return The identified <code>Node</code>, or <code>null</code> if no <code>Node</code> child could be located.
     */
    public static Node findElementWithNameAndNamespace(Node root, String name, String namespace) {
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            // see if child is element, and if so, has local name equal to string given
            Node child = nodeList.item(i);
            String n = child.getLocalName() == null ? child.getNodeName() : child.getLocalName();
            String ns = child.getNamespaceURI();

            // Ensure this is a element
            if (child.getNodeType() == Node.ELEMENT_NODE) {

                // Equal namespace and local name:
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

    /**
     * Translates a list of {@link javax.xml.namespace.QName}s to a {@link org.w3c.dom.Node}. The returned
     * <code>Node</code> has topic attribute set to <code>true</code>. None of the parents of the node has any children
     * of any type (element, attribute etc.) except the element leading to the <code>Node</code> with the topic
     * attribute.
     *
     * @param nameList the list of <code>QName</code>s to translate to a topic <code>Node</code>
     * @return The topic represented as a {@link org.w3c.dom.Node}. The parent of the returned node is assumed to be
     * a {@link org.oasis_open.docs.wsn.t_1.TopicSetType}
     */
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

    /**
     * Adds and sets the topic attribute on the given {@link org.w3c.dom.Node}.
     *
     * @param node the <code>Node</code> to give and set the topic attribute on
     * @throws java.lang.IllegalArgumentException if the {@link org.w3c.dom.Node} is not
     *                                            a {@link org.w3c.dom.Node#ELEMENT_NODE}
     */
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

    /**
     * Checks if a given node has the topic attribute. The attribute must have namespace
     * <code>http://docs.oasis-open.org/wsn/t-1</code>, as defined by OASIS wsn-ws_topics-1.3-spec-os.
     *
     * @param node the {@link org.w3c.dom.Node} to check
     * @return <code>true</code> if topic attribute is present and true. <code>false</code> otherwise.
     */
    public static boolean isTopic(Node node) {
        if (node == null)
            return false;
        NamedNodeMap nodeMap = node.getAttributes();
        if (nodeMap == null)
            return false;
        Node topicAttr = nodeMap.getNamedItemNS(WS_TOPIC_NAMESPACE, "topic");
        return topicAttr != null && topicAttr.getTextContent().equalsIgnoreCase("true");
    }

    /**
     * Extracts the expression defined in this {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} as a
     * {@link java.lang.String}. Will fail if there is no <code>String</code> in this topicExpression, or if the content
     * of the topicExpression is a more complex type.
     *
     * @param topicExpressionType The {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} to extract expression from.
     * @return the expression
     * @throws InvalidTopicExpressionFault if the <code>TopicExpressionType</code> does not have any content, or if the
     *                                     content is not a simple <code>String</code>.
     */
    public static String extractExpression(TopicExpressionType topicExpressionType) throws InvalidTopicExpressionFault {
        String expression = null;
        for (Object o : topicExpressionType.getContent()) {
            if (o instanceof String) {
                if (expression != null) {
                    throwInvalidTopicExpressionFault("en", "The given expression was not a simple expression!");
                }
                expression = (String) o;
            }
        }
        if (expression == null) {
            throwInvalidTopicExpressionFault("en", "No expression was given, and thus can not be evaluated!");
        }
        return expression == null ? null : expression.trim();
    }

    /**
     * Will build and throw an {@link org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault}.
     *
     * @param lang the language message has {@link org.oasis_open.docs.wsrf.bf_2.BaseFaultType.Description#setLang(String)}
     * @param desc the description, {@link org.oasis_open.docs.wsrf.bf_2.BaseFaultType.Description#setValue(String)}
     * @throws InvalidTopicExpressionFault the exception is thrown
     */
    public static void throwInvalidTopicExpressionFault(String lang, String desc) throws InvalidTopicExpressionFault {
        InvalidTopicExpressionFaultType faultType = new InvalidTopicExpressionFaultType();
        faultType.setTimestamp(new XMLGregorianCalendarImpl(new GregorianCalendar(TimeZone.getTimeZone("UTC"))));
        BaseFaultType.Description description = new BaseFaultType.Description();
        description.setLang(lang);
        description.setValue(desc);
        faultType.getDescription().add(description);
        throw new InvalidTopicExpressionFault(desc, faultType);
    }

    /**
     * Will build and throw a {@link org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault}.
     *
     * @param lang the language message has {@link org.oasis_open.docs.wsrf.bf_2.BaseFaultType.Description#setLang(String)}
     * @param desc the description, {@link org.oasis_open.docs.wsrf.bf_2.BaseFaultType.Description#setValue(String)}
     * @throws TopicExpressionDialectUnknownFault the exception is thrown
     */
    public static void throwTopicExpressionDialectUnknownFault(String lang, String desc)
            throws TopicExpressionDialectUnknownFault {
        TopicExpressionDialectUnknownFaultType faultType = new TopicExpressionDialectUnknownFaultType();
        faultType.setTimestamp(new XMLGregorianCalendarImpl(new GregorianCalendar(TimeZone.getTimeZone("UTC"))));
        BaseFaultType.Description description = new BaseFaultType.Description();
        description.setLang(lang);
        description.setValue(desc);
        faultType.getDescription().add(description);
        throw new TopicExpressionDialectUnknownFault("desc", faultType);
    }

    public static boolean isNCName(String ncName) {
        if (ncName == null || ncName.length() == 0)
            return false;

        if (!isNCStartChar(ncName.charAt(0)))
            return false;

        for (int i = 1; i < ncName.length(); i++) {
            if (!isNCChar(ncName.charAt(i)))
                return false;
        }
        return true;
    }

    public static boolean isNCStartChar(char c) {
        // Upper case
        if (c >= 'A' && c <= 'Z')
            return true;
        // lower case
        if (c >= 'a' && c <= 'z')
            return true;
        // underscore
        return c == '_';
    }

    public static boolean isNCChar(char c) {
        // hyphen and punctuation
        if (c == '-' || c == '.')
            return true;
        // Number
        return c >= '0' && c <= '9' || isNCStartChar(c);
    }

    public static String topicToString(List<QName> topic) {
        if (topic == null || topic.size() == 0)
            return null;

        String returnString = topic.get(0).toString();

        for (int i = 1; i < topic.size(); i++) {
            returnString += "/" + topic.get(i).toString();
        }

        return returnString;
    }
}
