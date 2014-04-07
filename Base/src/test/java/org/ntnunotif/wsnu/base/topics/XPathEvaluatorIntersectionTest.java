package org.ntnunotif.wsnu.base.topics;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.NuNamespaceContext;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.topics.XPathEvaluator;
import org.oasis_open.docs.wsn.t_1.TopicSetType;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 06.04.2014.
 */
public class XPathEvaluatorIntersectionTest {

    private NamespaceContext namespaceContext;
    private TopicSetType topicSetType;

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
