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

package org.ntnunotif.wsnu.examples.base;

import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.util.Log;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * A short example on the built in functions for topic node manipulations
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
