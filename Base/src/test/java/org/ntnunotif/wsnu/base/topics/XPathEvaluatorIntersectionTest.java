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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.NuNamespaceContext;
import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.t_1.TopicSetType;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class XPathEvaluatorIntersectionTest {

    private NamespaceContext namespaceContext;
    private TopicSetType topicSetType;

    @BeforeClass
    public static void setUpClass(){
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);
    }

    @Before
    public void setUp() {
        NuNamespaceContext context = new NuNamespaceContext();
        context.put("test", "http://test.com");
        context.put("ex", "http://example.org");
        context.put("wstop", "http://docs.oasis-open.org/wsn/t-1");
        namespaceContext = context;

        List<List<QName>> topicSetSource = new ArrayList<>();
        List<QName> topic1 = new ArrayList<>();
        topic1.add(new QName("http://example.org", "r"));
        topic1.add(new QName("c"));
        List<QName> topic2 = new ArrayList<>();
        topic2.add(new QName("http://test.com", "r2"));
        topicSetSource.add(topic1);
        topicSetSource.add(topic2);
        topicSetType = TopicUtils.qNameListListToTopicSet(topicSetSource);
    }

    @Test
    public void testStaticIntersectionOneHit() throws Exception {
        TopicSetType result = XPathEvaluator.getXpathIntersection("//c",topicSetType, namespaceContext);
        Assert.assertNotNull("failed simple intersection", result);
        Assert.assertEquals("Wrong number of topics returned" , 1, result.getAny().size());
        List<QName> expected = new ArrayList<>();
        expected.add(new QName("http://example.org", "r"));
        expected.add(new QName("c"));
        List<List<QName>> exp = new ArrayList<>();
        exp.add(expected);
        Assert.assertEquals("Wrong topics returned", exp, TopicUtils.topicSetToQNameList(result,true));
    }

    @Test
    public void testStaticIntersectionWildcards() throws Exception {
        TopicSetType result = XPathEvaluator.getXpathIntersection("//*",topicSetType, namespaceContext);
        Assert.assertNotNull("failed simple intersection", result);
        Assert.assertEquals("Wrong number of topics returned" , 2, result.getAny().size());

        List<List<QName>> topicSetSource = new ArrayList<>();
        List<QName> topic1 = new ArrayList<>();
        topic1.add(new QName("http://example.org", "r"));
        topic1.add(new QName("c"));
        List<QName> topic2 = new ArrayList<>();
        topic2.add(new QName("http://test.com", "r2"));
        topicSetSource.add(topic1);
        topicSetSource.add(topic2);

        for (List<QName> returnNames : TopicUtils.topicSetToQNameList(result, true)) {
            Assert.assertTrue("Topic was not present", topicSetSource.contains(returnNames));
        }
    }

    @Test
    public void testStaticIntersectionNull() throws Exception {
        TopicSetType result = XPathEvaluator.getXpathIntersection("//something//*",topicSetType, namespaceContext);
        Assert.assertNull("failed simple intersection", result);
    }


    @Test
    public void testStaticIntersectionWSTop() throws Exception {
        TopicSetType result = XPathEvaluator.getXpathIntersection("//*[@wstop:topic=\"true\"]",topicSetType, namespaceContext);
        Assert.assertNotNull("failed simple intersection", result);
        Assert.assertEquals("Wrong number of topics returned" , 2, result.getAny().size());

        List<List<QName>> topicSetSource = new ArrayList<>();
        List<QName> topic1 = new ArrayList<>();
        topic1.add(new QName("http://example.org", "r"));
        topic1.add(new QName("c"));
        List<QName> topic2 = new ArrayList<>();
        topic2.add(new QName("http://test.com", "r2"));
        topicSetSource.add(topic1);
        topicSetSource.add(topic2);

        for (List<QName> returnNames : TopicUtils.topicSetToQNameList(result, true)) {
            Assert.assertTrue("Topic was not present", topicSetSource.contains(returnNames));
        }

        result = XPathEvaluator.getXpathIntersection("//ex:r//*[@wstop:topic=\"true\"]",topicSetType, namespaceContext);
        Assert.assertNotNull("failed xpath intersection", result);
        Assert.assertEquals("Wrong number of topics returned" , 1, result.getAny().size());
        for (List<QName> returnNames : TopicUtils.topicSetToQNameList(result, true)) {
            Assert.assertTrue("Topic was not present", topicSetSource.contains(returnNames));
        }
    }
}
