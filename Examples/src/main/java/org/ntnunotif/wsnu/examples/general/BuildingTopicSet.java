package org.ntnunotif.wsnu.examples.general;

import org.oasis_open.docs.wsn.t_1.TopicSetType;

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
        //
    }

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

        List<QName> fouthTopicList = new ArrayList<>();
        fouthTopicList.add(new QName(topicNamespace1, rootTopic2));

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

        // We can create a factory, and a topic set we shall fill
        org.oasis_open.docs.wsn.t_1.ObjectFactory factory = new org.oasis_open.docs.wsn.t_1.ObjectFactory();
        TopicSetType topicSetType = factory.createTopicSetType();

        // returning the set
        return topicSetType;
    }
}
