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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TopicUtilsTest {
    private static List<List<QName>> fullQNameList;

    private static final String[] namespaces = { "http://test1.com", "http://test2.com" };
    private static final String[] localNames = { "test", "test1", "test2" };
    private static final String[] rootNSs = {"http://root_test1.com", "http://root_test2.com", "http://root_test3.com" };
    private static final String[] rootLocalNames = {"roo1", "root2" };

    private static final String OUTQNameToTopicSetRes = "/out_topic_utils_qnames_topic_set.xml";
    private static final String OUTQNameListTranslateRes = "/out_topic_utils_translate_list_to_expression.xml";

    @BeforeClass
    public static void setup() {
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        fullQNameList = new ArrayList<>();
        List<QName> qNameList= new ArrayList<>();
        for (String ns: namespaces) {
            for (String loc: localNames) {
                qNameList.add(new QName(ns,loc));
            }
        }
        List<QName> rootList = new ArrayList<>();
        for (String ns: rootNSs) {
            for (String loc: rootLocalNames) {
                rootList.add(new QName(ns, loc));
            }
        }
        // 6 root topics, 18 full topic trees, combining to 24 topics
        for (QName root: rootList) {
            for (String loc: localNames) {
                List<QName> curList = new ArrayList<>();
                curList.add(root);
                curList.add(new QName(loc));
                curList.addAll(qNameList);
                fullQNameList.add(curList);
            }
        }
        // Add the root topics themselves as topics, making tests more effective (we know implementation details).
        for (QName root: rootList) {
            List<QName> curList = new ArrayList<>();
            curList.add(root);
            fullQNameList.add(curList);
        }
    }

    @Test
    public void testQNameListToTopicSetToQNameList() {
        TopicSetType topicSetType = TopicUtils.qNameListListToTopicSet(fullQNameList);
        Assert.assertNotNull("Topic set returned was null!", topicSetType);

        List<List<QName>> translatedRootList = TopicUtils.topicSetToQNameList(topicSetType, false);
        List<List<QName>> translatedFullList = TopicUtils.topicSetToQNameList(topicSetType, true);

        Assert.assertNotNull("QNames returned from root topics was null!", translatedRootList);
        Assert.assertNotNull("QNames returned from full topic list was null!", translatedFullList);

        Assert.assertEquals("QNames returned from root topics was wrong amount", 6, translatedRootList.size());
        Assert.assertEquals("QNames returned from full topic list was wrong amount", fullQNameList.size(), translatedFullList.size());
        Assert.assertEquals("QNames returned from full topic list was wrong amount", 24, translatedFullList.size());

        for (int i = 0; i < translatedRootList.size(); i++) {
            List<QName> name = translatedRootList.get(i);
            Assert.assertNotNull("A QName list in root list was null", name);
            Assert.assertTrue("QName List with root " + name.get(0) + " from root list was not in original list", fullQNameList.contains(name));
        }
        for (int i = 0; i < translatedFullList.size(); i++) {
            List<QName> name = translatedFullList.get(0);
            Assert.assertNotNull("A QName list in full list was null!", name);
            Assert.assertTrue("QName List with root " + name.get(0) + " from full list was not in original list", fullQNameList.contains(name));
        }

        for (int i = 0; i < translatedRootList.size() - 1; i++) {
            List<QName> first = translatedRootList.get(i);
            for (int j = i + 1; j < translatedRootList.size(); j++) {
                List<QName> second = translatedRootList.get(j);
                Assert.assertFalse("QName List with root " + first.get(0) + " was duplicated in root transformation!", first.equals(second));
            }
        }

        for (int i = 0; i < translatedFullList.size() - 1; i++) {
            List<QName> first = translatedFullList.get(i);
            for (int j = i + 1; j < translatedFullList.size(); j++) {
                List<QName> second = translatedFullList.get(j);
                Assert.assertFalse("QName List with root " + first.get(0) + " was duplicated in full list transformation!", first.equals(second));
            }
        }
    }

    @Test
    public void testOutputOfQNameToTopicSet() throws Exception{
        TopicSetType topicSetType = TopicUtils.qNameListListToTopicSet(fullQNameList);

        // Must not be null
        Assert.assertNotNull("Topic set built from qnames was null!", topicSetType);

        // Write to file, so it is possible to see actual content of returned set
        JAXBElement e = new JAXBElement<>(new QName("http://docs.oasis-open.org/wsn/t-1", "TopicSet"), TopicSetType.class, topicSetType);
        XMLParser.writeObjectToStream(e, new FileOutputStream(getClass().getResource(OUTQNameToTopicSetRes).getFile()));
    }

    @Test
    public void testTranslateQNameListTopicToTopicExpression() throws Exception {
        //
        List<QName> qNames = new ArrayList<>();
        qNames.add(new QName("http://www.example.com", "localName", "pre"));
        TopicExpressionType topicExpressionType = TopicUtils.translateQNameListTopicToTopicExpression(qNames);

        Assert.assertEquals("Wrong amount of things attributes added to topic", 1, topicExpressionType.getOtherAttributes().size());

        ObjectFactory factory = new ObjectFactory();
        Object parseable = factory.createTopicExpression(topicExpressionType);
        XMLParser.writeObjectToStream(parseable, new ByteArrayOutputStream());
        XMLParser.writeObjectToStream(parseable, new FileOutputStream(getClass().getResource(OUTQNameListTranslateRes).getFile()));
    }
}
