package org.ntnunotif.wsnu.examples.general;

import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.util.Log;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * A short example on the built in functions for topic node manipulations
 * Created by Inge on 07.05.2014.
 */
public class TopicNodeManipulation {

    public static void main(String[] args) {
        // Disable logging
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        // Create a list of QName representing one topic
        List<QName> topicList = new ArrayList<>();
        topicList.add(new QName(BuildingTopicSet.TOPIC_NAMESPACE_1, BuildingTopicSet.ROOT_TOPIC_1));

        // Create a topic node of the list
        Node topicNode = TopicUtils.qNameListToTopicNode(topicList);

        // See if a node is a topic node
        if (TopicUtils.isTopic(topicNode)) {
            System.out.println("the node was a topic");
        } else {
            System.err.println("The node was not a topic");
        }

        // Force a node to not be a topic node
        TopicUtils.forceNonTopicNode(topicNode);
        if (TopicUtils.isTopic(topicNode)) {
            System.err.println("the node was a topic");
        } else {
            System.out.println("The node was not a topic");
        }

        // Make a node a topic node
        TopicUtils.makeTopicNode(topicNode);
        if (TopicUtils.isTopic(topicNode)) {
            System.out.println("the node was a topic");
        } else {
            System.err.println("The node was not a topic");
        }
    }
}
