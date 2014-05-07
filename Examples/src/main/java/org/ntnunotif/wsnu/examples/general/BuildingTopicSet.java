package org.ntnunotif.wsnu.examples.general;

import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an example on how one can build a {@link org.oasis_open.docs.wsn.t_1.TopicSetType}, in code, representing a set of topics as described in OASIS WS-Topics 1.3 specification.
 * Created by Inge on 07.05.2014.
 */
public class BuildingTopicSet {

    public static final String topicNamespace1 = "http://example.com/topic/ns1";
    public static final String topicNamespace2 = "http://example.com/topic/second_namespace";

    public static final String rootTopic1 = "root_topic_1";
    public static final String rootTopic2 = "root_topic_2";

    public static final String childTopic1 = "child_topic_1";
    public static final String childTopic2 = "child_topic_2";

    public static final String nonTopicNode = "non-topic";

    public static void main(String[] args) {

        // Before anything, we turn of logging in project, we really do not want all output for this example
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        // This example first builds a TopicSetType
        TopicSetType topicSetType = buildTopicSet();

        // Then print it
        printTopicSetType(topicSetType);

        // The topics are properly nested in their nodes, there should be 5 root nodes in this set
        int size = topicSetType.getAny().size();
        if (size != 5) {
            System.err.println("Expected 5 topic root nodes, but found: " + size);
        }

        // A TopicSetType is not directly parseable, it is not marked as a root element.
        // If we wrap the TopicSetType in JAXBElement, it should be easy marshaled using the built in parser:
        org.oasis_open.docs.wsn.t_1.ObjectFactory factory = new org.oasis_open.docs.wsn.t_1.ObjectFactory();
        JAXBElement<TopicSetType> setAsElement = factory.createTopicSet(topicSetType);

        try {
            XMLParser.writeObjectToStream(setAsElement, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
            System.err.println("Topic set could not be parsed!");
        }
    }

    /**
     * Builds a topic set containing exactly eight topics. The topics full names are on the form {namespace}localname.
     * <ul>
     * <li>{http://example.com/topic/second_namespace}root_topic_1</li>
     * <li>{http://example.com/topic/second_namespace}root_topic_1/{http://example.com/topic/ns1}child_topic_1/non-topic/{http://example.com/topic/second_namespace}child_topic_1</li>
     * <li>{http://example.com/topic/ns1}non-topic/{http://example.com/topic/ns1}child_topic_2</li>
     * <li>{http://example.com/topic/ns1}non-topic/{http://example.com/topic/ns1}child_topic_1</li>
     * <li>{http://example.com/topic/ns1}root_topic_2</li>
     * <li>{http://example.com/topic/ns1}root_topic_1</li>
     * <li>root_topic_1</li>
     * <li>root_topic_1/non-topic/child_topic_1</li>
     * </ul>
     *
     * @return A topic set with eight topics
     */
    public static TopicSetType buildTopicSet() {
        // A topic may be represented as a list of QNames. We start to represent all topics we want in our set as lists

        List<QName> firstTopicList = new ArrayList<>();
        // When building undocumented sets, it is legal to add topics which are not defined in any specific namespace.
        firstTopicList.add(new QName(rootTopic1));
        // This list now represents a complete topic. We can build longer lists that represent single topics

        // Longer lists represents single topics as well. They may be emerging from the ad-hoc topics, namespaced root
        // topics, or just empty nodes representing a path to the topic. Let us create some.
        List<QName> secondTopicList = new ArrayList<>();
        secondTopicList.add(new QName(rootTopic1));
        secondTopicList.add(new QName(nonTopicNode));
        secondTopicList.add(new QName(childTopic1));

        List<QName> thirdTopicList = new ArrayList<>();
        thirdTopicList.add(new QName(topicNamespace1, rootTopic1));

        List<QName> fourthTopicList = new ArrayList<>();
        fourthTopicList.add(new QName(topicNamespace1, rootTopic2));

        List<QName> fifthTopicList = new ArrayList<>();
        fifthTopicList.add(new QName(topicNamespace1, nonTopicNode));
        fifthTopicList.add(new QName(topicNamespace1, childTopic1));

        List<QName> sixthTopicList = new ArrayList<>();
        sixthTopicList.add(new QName(topicNamespace1, nonTopicNode));
        sixthTopicList.add(new QName(topicNamespace1, childTopic2));

        List<QName> seventhTopicList = new ArrayList<>();
        seventhTopicList.add(new QName(topicNamespace2, rootTopic1));

        // We can extend a topic with children that even are from a different namespace
        List<QName> eightTopicList = new ArrayList<>();
        eightTopicList.add(new QName(topicNamespace2, rootTopic1));
        eightTopicList.add(new QName(topicNamespace1, childTopic1));
        eightTopicList.add(new QName(nonTopicNode));
        eightTopicList.add(new QName(topicNamespace2, childTopic1));


        // After this we have two ways we can go to create the topic set. Let us start at the most complex way.
        // This is the way to go if you need to extend the set later on

        // We can create a factory, and a topic set we shall fill
        org.oasis_open.docs.wsn.t_1.ObjectFactory factory = new org.oasis_open.docs.wsn.t_1.ObjectFactory();
        TopicSetType topicSetType = factory.createTopicSetType();

        // Now, we can create some w3c node representations of topics to put in this set.
        // This is done by using utility functions in TopicUtils
        Node firstTopicNode = TopicUtils.qNameListToTopicNode(firstTopicList);
        Node secondTopicNode = TopicUtils.qNameListToTopicNode(secondTopicList);

        // We now can add them to the set, using TopicUtils
        TopicUtils.addTopicToTopicSet(firstTopicNode, topicSetType);
        TopicUtils.addTopicToTopicSet(secondTopicNode, topicSetType);

        // We can even add lists of QNames directly
        TopicUtils.addTopicToTopicSet(thirdTopicList, topicSetType);

        // To check what we have done, we count the members of the set. There should be two root nodes, first and second
        // topic share the same root
        int size = topicSetType.getAny().size();
        if (size != 2) {
            // This should not be so
            System.err.println("Expected two node, but found: " + topicSetType.getAny().size());
        }

        // If we all topics in this set to a lists of topics represented by QNames, we would expect three topics.
        size = TopicUtils.topicSetToQNameList(topicSetType, true).size();
        if (size != 3) {
            // This should not be so
            System.err.println("Expected 3 unique topics, but found: " + size);
        }


        // However, there is an alternative to this way of building the set.
        // If we have all topics in a list of lists of QNames, we can get the set directly using TopicUtils

        List<List<QName>> setList = new ArrayList<>();
        setList.add(firstTopicList);
        setList.add(secondTopicList);
        setList.add(thirdTopicList);
        setList.add(fourthTopicList);
        setList.add(fifthTopicList);
        setList.add(sixthTopicList);
        setList.add(seventhTopicList);
        setList.add(eightTopicList);

        // The complete set can now be built using a single method in TopicUtils
        topicSetType = TopicUtils.qNameListListToTopicSet(setList);

        // returning the set
        return topicSetType;
    }

    public static void printTopicSetType(TopicSetType topicSetType) {
        // To print all topics in this set, we first would like to convert it to a more readable form.
        // This can be easily achieved using list of lists of QNames, and a convenient method exists in TopicUtils.
        List<List<QName>> topicSetList = TopicUtils.topicSetToQNameList(topicSetType, true);

        // To print we simply iterate through the list of lists of QNames, and use the convenient conversion from
        // a list of QNames representing a topic, to a string

        for (List<QName> list : topicSetList) {
            System.out.println(TopicUtils.topicToString(list));
        }
    }
}
