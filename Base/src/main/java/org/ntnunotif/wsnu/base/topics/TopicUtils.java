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
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsrf.bf_2.BaseFaultType;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

/**
 * Helper methods to use with topics. Should comply to OASIS standard wsn-ws_topics-1.3-spec-os.
 *
 * @author Inge Edward Halsaunet
 */
public class TopicUtils {

    /**
     * WS Topic Namespace, defined by OASIS
     */
    public static final String WS_TOPIC_NAMESPACE = "http://docs.oasis-open.org/wsn/t-1";

    private static int tnsno = 1;

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
        Log.d("TopicUtils", "topicSetToQNameList called, recursive: " + recursive);
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
        Log.d("TopicUtils", "private non recursive method called");
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
        Log.d("TopicUtils", "Private recursive method called");
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
        Log.d("TopicUtils", "qNameListListToTopicSet called with list sized: " + (names == null ? "null" : names.size()));
        if (names == null)
            return null;

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

        Log.d("TopicUtils", "topicNodeToQNameList called.");
        List<QName> qNames = new ArrayList<>();
        Node current = topicNode;

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
        Log.d("TopicUtils", "addTopicToTopicSet(List<QName>, TopicSetType) called.");
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
        Log.d("TopicUtils", "addTopicToTopicSet(Node, TopicSetType) called");
        if (!isTopic(topic))
            throw new IllegalArgumentException("Tried to add a non-topic to a topic set!");

        Log.d("TopicUtils", "Finding root topic.");
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

        Log.d("TopicUtils", "Finding node to merge from");
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
            Log.d("TopicUtils", "Node added");
            topicSet.getAny().add(current);
            return;
        }

        Log.d("TopicUtils", "Merging");
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
        Log.d("TopicUtils", "findElementWithNameAndNamespace called");
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
        Log.d("TopicUtils", "qNameListToTopicNode called");
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
        Log.d("TopicUtils", "makeTopicNode called");

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
     * Forces a node to not be a topic node. That is, if it previously had the wnst:topic attribute set, this attribute
     * is set to <code>false</code>. If it did not have this attribute set, the method does nothing.
     *
     * @param node the {@link org.w3c.dom.Node} to change
     */
    public static void forceNonTopicNode(Node node) {
        Log.d("TopicUtils", "forceNonTopicNode called");

        if (node == null)
            return;

        if (node.getNodeType() != Node.ELEMENT_NODE)
            throw new IllegalArgumentException("Tried to force a non-element node non-topic!");

        Element element = (Element) node;
        Attr topicAttr = element.getAttributeNodeNS(WS_TOPIC_NAMESPACE, "topic");
        if (topicAttr != null)
            topicAttr.setValue("false");
    }

    /**
     * Checks if a given node has the topic attribute. The attribute must have namespace
     * <code>http://docs.oasis-open.org/wsn/t-1</code>, as defined by OASIS wsn-ws_topics-1.3-spec-os.
     *
     * @param node the {@link org.w3c.dom.Node} to check
     * @return <code>true</code> if topic attribute is present and true. <code>false</code> otherwise.
     */
    public static boolean isTopic(Node node) {
        Log.d("TopicUtils", "isTopic called");
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
        Log.d("TopicUtils", "extractExpression called");
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
        Log.d("TopicUtils", "Throwing InvalidTopicExpressionFault: " + desc);

        InvalidTopicExpressionFaultType faultType = new InvalidTopicExpressionFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            faultType.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("TopicUtils", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }
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
        Log.d("TopicUtils", "Throwing TopicExpressionDialectUnknownFault: " + desc);

        TopicExpressionDialectUnknownFaultType faultType = new TopicExpressionDialectUnknownFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            faultType.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("TopicUtils", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }
        BaseFaultType.Description description = new BaseFaultType.Description();
        description.setLang(lang);
        description.setValue(desc);
        faultType.getDescription().add(description);
        throw new TopicExpressionDialectUnknownFault(desc, faultType);
    }

    /**
     * Will build and throw a {@link org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault}.
     *
     * @param lang the language message has {@link org.oasis_open.docs.wsrf.bf_2.BaseFaultType.Description#setLang(String)}
     * @param desc the description, {@link org.oasis_open.docs.wsrf.bf_2.BaseFaultType.Description#setValue(String)}
     * @throws MultipleTopicsSpecifiedFault the exception is thrown
     */
    public static void throwMultipleTopicsSpecifiedFault(String lang, String desc) throws MultipleTopicsSpecifiedFault {
        Log.d("TopicUtils", "Throwing MultipleTopicsSpecifiedFault: " + desc);

        MultipleTopicsSpecifiedFaultType faultType = new MultipleTopicsSpecifiedFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            faultType.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("TopicUtils", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }
        BaseFaultType.Description description = new BaseFaultType.Description();
        description.setLang(lang);
        description.setValue(desc);
        faultType.getDescription().add(description);
        throw new MultipleTopicsSpecifiedFault(desc, faultType);
    }

    /**
     * Checks if the given {@link java.lang.String} is a valid NCName.
     *
     * @param ncName the <code>String</code> to check
     * @return <code>true</code> if this is a NCName. <code>false</code> otherwise.
     */
    public static boolean isNCName(String ncName) {
        Log.d("TopicUtils", "isNCName called on: " + ncName);

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

    /**
     * Checks if the given <code>char</code> is acceptable as a start letter of a NCName.
     *
     * @param c the <code>char</code> to check
     * @return <code>true</code> if it is acceptable. <code>false</code> otherwise.
     */
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

    /**
     * Checks if the given <code>char</code> is allowed in NCNames. This includes both start characters and mid- word
     * characters.
     *
     * @param c the <code>char</code> to check
     * @return <code>true</code> if it is acceptable. <code>false</code> otherwise.
     */
    public static boolean isNCChar(char c) {
        // hyphen and punctuation
        if (c == '-' || c == '.')
            return true;
        // Number
        return (c >= '0' && c <= '9') || isNCStartChar(c);
    }

    /**
     * Translates the topic, given as a list of {@link javax.xml.namespace.QName}s to a easy read variation. It is
     * represented as multiple <code>{Namespace}:local</code> separated by <code>/</code>.
     *
     * @param topic the topic to represent as a {@link java.lang.String}
     * @return the <code>String</code> representation of the topic
     */
    public static String topicToString(List<QName> topic) {
        Log.d("TopicUtils", "topicToString called");

        if (topic == null || topic.size() == 0)
            return null;

        String returnString = topic.get(0).toString();

        for (int i = 1; i < topic.size(); i++) {
            returnString += "/" + topic.get(i).toString();
        }

        return returnString;
    }

    /**
     * Translates a list of {@link javax.xml.namespace.QName} to a
     * {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}. The dialect chosen for the
     * <code>TopicExpressionType</code> will be either the dialect for simple or concrete topic. The choice depends on
     * how many <code>QName</code>s is in the list. 1 <code>QName</code> results in simple dialect. More results in
     * concrete.
     *
     * @param topic the topic represented as a list of QNames
     * @return the topic represented by a <code>TopicExpressionType</code>
     */
    public static TopicExpressionType translateQNameListTopicToTopicExpression(List<QName> topic) {

        Log.d("TopicUtils", "translateQNameListTopicToTopicExpression called");

        if (topic == null || topic.size() < 1) {
            Log.w("TopicUtils", "Tried to convert empty list to topic expression");
            return null;
        }

        ObjectFactory factory = new ObjectFactory();
        TopicExpressionType topicExpressionType = factory.createTopicExpressionType();
        String expression = null;

        if (topic.size() == 1) {
            // Simple Topic expression dialect
            topicExpressionType.setDialect(SimpleEvaluator.dialectURI);
        } else {
            // Concrete Topic Expression dialect
            topicExpressionType.setDialect(ConcreteEvaluator.dialectURI);
        }

        for (QName qName : topic) {

            if (qName.getNamespaceURI() == null || qName.getNamespaceURI().equals(XMLConstants.NULL_NS_URI)) {
                // Null namespace, only local part is relevant. This is a NCName, and should be used directly
                expression = expression == null ? qName.getLocalPart() : expression + "/" + qName.getLocalPart();
            } else {
                // Namespace is not null. Retain namespace information in node.

                String prefix;
                if (qName.getPrefix() == null || qName.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                    // There is not stored a prefix in the qname, create new
                    prefix = "tnsg" + tnsno++;
                    Log.d("TopicUtils", "A automatic prefix for a QName was generated: " + prefix);
                } else
                    prefix = qName.getPrefix();

                // check if this is the default xmlns attribute
                if (!prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                    // add namespace context to the expression node
                    topicExpressionType.getOtherAttributes().put(new QName("xmlns:" + prefix), qName.getNamespaceURI());
                }

                // Add the prefixed name to the expression
                String name = prefix + ":" + qName.getLocalPart();
                expression = expression == null ? name : expression + "/" + name;
            }
        }

        topicExpressionType.getContent().add(expression);
        Log.d("TopicUtils", "A topic as a QName list was translated to a topic expression: " + expression);
        return topicExpressionType;
    }
}
