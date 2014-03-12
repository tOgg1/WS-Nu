package ntnunotif.wsnu.base.topics;

import org.junit.Test;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.t_1.TopicNamespaceType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * <code>TopicValidatorTest</code> tests the <code>TopicValidator</code>. It is dependent on <code>XMLParser</code>
 * Created by Inge on 06.03.14.
 */
public class TopicValidatorTest {
    private final String gcmXPathMulPath = "Base/test/ntnunotif/wsnu/base/topics/topic_gcm_xpath_boolean_multiple_test.xml";
    private final String gcmXPathSinPath = "Base/test/ntnunotif/wsnu/base/topics/topic_gcm_xpath_boolean_single_test.xml";
    private final String gcmXPathFalsePath = "Base/test/ntnunotif/wsnu/base/topics/topic_gcm_xpath_false.xml";
    private final String topicNamespace = "Base/test/ntnunotif/wsnu/base/topics/topic_namespace_test.xml";
    private final String topicSet = "Base/test/ntnunotif/wsnu/base/topics/topic_set_test.xml";

    @Test
    public void parseXML() throws Exception {
        FileInputStream fis = new FileInputStream(gcmXPathFalsePath);
        GetCurrentMessage msg = (GetCurrentMessage)XMLParser.parse(fis);
        fis.close();
        fis = new FileInputStream(gcmXPathMulPath);
        msg = (GetCurrentMessage)XMLParser.parse(fis);
        fis.close();
        fis = new FileInputStream(gcmXPathSinPath);
        msg = (GetCurrentMessage)XMLParser.parse(fis);
        fis.close();
        fis = new FileInputStream(topicNamespace);
        JAXBElement ns = (JAXBElement)XMLParser.parse(fis);
        fis.close();
        fis = new FileInputStream(topicSet);
        JAXBElement ts = (JAXBElement)XMLParser.parse(fis);
        fis.close();
    }

    @Test
    public void testTopicValidation() throws Exception{
        File file = new File("Base/test/ntnunotif/wsnu/base/topics/topics_gcm_");
        FileInputStream fileInputStream = new FileInputStream(file);
        Object parsedObject = XMLParser.parse(fileInputStream);
        // TODO Write a testing xml file and some testing code
    }
}
