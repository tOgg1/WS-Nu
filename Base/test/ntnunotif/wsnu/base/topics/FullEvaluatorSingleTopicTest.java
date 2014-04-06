package ntnunotif.wsnu.base.topics;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.NuNamespaceContext;
import org.ntnunotif.wsnu.base.topics.FullEvaluator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 06.04.2014.
 */
public class FullEvaluatorSingleTopicTest {
    private NamespaceContext namespaceContext;

    @Before
    public void setUp() {
        NuNamespaceContext context = new NuNamespaceContext();
        context.put("test", "http://test.com");
        context.put("ex", "http://example.org");
        namespaceContext = context;
    }

    @Test
    public void testSimpleTopicTranslation() throws Exception{
        List<QName> expected = new ArrayList<>();
        expected.add(new QName("http://example.org", "root"));
        List<QName> qNames = FullEvaluator.evaluateFullTopicExpressionToQNameList("ex:root", namespaceContext);
        Assert.assertEquals("Simple expression failed", expected, qNames);
    }

    @Test
    public void testLongerTopicTranslation() throws Exception{
        List<QName> expected = new ArrayList<>();
        expected.add(new QName("http://example.org", "root"));
        expected.add(new QName("haha"));
        expected.add(new QName("http://test.com", "b"));
        List<QName> qNames = FullEvaluator.evaluateFullTopicExpressionToQNameList("ex:root/haha/test:b", namespaceContext);
        Assert.assertEquals("Simple expression failed", expected, qNames);
    }
}
