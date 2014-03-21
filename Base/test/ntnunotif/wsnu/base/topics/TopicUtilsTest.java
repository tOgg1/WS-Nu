package ntnunotif.wsnu.base.topics;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.oasis_open.docs.wsn.t_1.TopicSetType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 21.03.14.
 */
public class TopicUtilsTest {
    private static List<QName> qNameList;

    private static final String[] namespaces = { "http://test1.com", "http://test2.com" };
    private static final String[] localNames = { "test", "test1", "test/test", "test1/test/test/test" };

    private static final String OUTQNameToTopicSet = "Base/test/ntnunotif/wsnu/base/topics/out_topic_utils_qnames_topic_set.xml";

    @BeforeClass
    public static void setup() {
        qNameList = new ArrayList<>();
        for (String ns: namespaces) {
            for (String loc: localNames) {
                qNameList.add(new QName(ns,loc));
            }
        }
    }

    @Test
    public void testQNameListToTopicSetToQNameList() {
        TopicSetType topicSetType = TopicUtils.qNameListToTopicSet(qNameList);
        Assert.assertNotNull("Topic set returned was null!", topicSetType);

        List<QName> translatedRootList = TopicUtils.topicSetToQNameList(topicSetType, false);
        List<QName> translatedFullList = TopicUtils.topicSetToQNameList(topicSetType, true);

        Assert.assertNotNull("QNames returned from root topics was null!", translatedRootList);
        Assert.assertNotNull("QNames returned from full topic list was null!", translatedFullList);

        Assert.assertEquals("QNames returned from root topics was wrong amount", 4, translatedRootList.size());
        Assert.assertEquals("QNames returned from full topic list was wrong amount", qNameList.size(), translatedFullList.size());

        for (QName name: translatedRootList)
            Assert.assertTrue("QName " + name + " from root list was not in original list", qNameList.contains(name));
        for (QName name: translatedFullList)
            Assert.assertTrue("QName " + name + " from full list was not in original list", qNameList.contains(name));

        for (int i = 0; i < translatedRootList.size() - 1; i++) {
            QName first = translatedRootList.get(i);
            for (int j = i + 1; j < translatedRootList.size(); j++) {
                QName second = translatedRootList.get(j);
                Assert.assertFalse("QName " + first + " was duplicated in root transformation!", first.equals(second));
            }
        }

        for (int i = 0; i < translatedFullList.size() - 1; i++) {
            QName first = translatedFullList.get(i);
            for (int j = i + 1; j < translatedFullList.size(); j++) {
                QName second = translatedFullList.get(j);
                Assert.assertFalse("QName " + first + " was duplicated in full list transformation!", first.equals(second));
            }
        }
    }

    @Test
    public void testOutputOfQNameToTopicSet() throws Exception{
        TopicSetType topicSetType = TopicUtils.qNameListToTopicSet(qNameList);

        // Must not be null
        Assert.assertNotNull("Topic set built from qnames was null!", topicSetType);

        // Write to file, so it is possible to see actual content of returned set
        JAXBElement e = new JAXBElement<>(new QName("http://docs.oasis-open.org/wsn/t-1", "TopicSet"), TopicSetType.class, topicSetType);
        XMLParser.writeObjectToStream(e, new FileOutputStream(OUTQNameToTopicSet));
    }
}
