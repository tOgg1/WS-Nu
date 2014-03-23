package ntnunotif.wsnu.base.topics;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.topics.TopicValidator;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicNamespaceType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * <code>TopicValidatorTest</code> tests the <code>TopicValidator</code>. It is dependent on <code>XMLParser</code>
 * Created by Inge on 06.03.14.
 */
public class TopicValidatorTest {
    private static final String gcmXPathMulPath = "Base/test/ntnunotif/wsnu/base/topics/topic_gcm_xpath_boolean_multiple_test.xml";
    private static final String gcmXPathSinPath = "Base/test/ntnunotif/wsnu/base/topics/topic_gcm_xpath_boolean_single_test.xml";
    private static final String gcmXPathFalsePath = "Base/test/ntnunotif/wsnu/base/topics/topic_gcm_xpath_false.xml";
    private static final String gcmIllegalDialectPath = "Base/test/ntnunotif/wsnu/base/topics/topic_gcm_illegal_dialect_test.xml";
    private static final String topicNamespacePath = "Base/test/ntnunotif/wsnu/base/topics/topic_namespace_test.xml";
    private static final String topicSetPath = "Base/test/ntnunotif/wsnu/base/topics/topic_set_test.xml";
    private static final String OUTGcmXPathMulPath = "Base/test/ntnunotif/wsnu/base/topics/out_topic_gcm_xpath_boolean_multiple_test.xml";
    private static final String OUTGcmXPathSinPath = "Base/test/ntnunotif/wsnu/base/topics/out_topic_gcm_xpath_boolean_single_test.xml";

    private static TopicExpressionType xPathMultipleHits;
    private static TopicExpressionType xPathSingleHit;
    private static TopicExpressionType xPathFalse;
    private static TopicExpressionType illegalExpressionDialect;
    private static TopicNamespaceType topicNamespace;
    private static TopicSetType topicSet;

    private static InternalMessage xPathMulMsg;
    private static InternalMessage xPathSinMsg;
    private static InternalMessage xPathFalMsg;
    private static InternalMessage illExprDiaMsg;
    private static InternalMessage topNSMsg;
    private static InternalMessage topSetMsg;

    private static final String testNamespace = "http://ws-nu.org/testTopicSpace1";
    private static final String testRootTopic1 = "root_topic1";
    private static final String testChildTopic = "root_topic1/child_topic";
    private static final String testRootTopic2 = "root_topic2";

    @BeforeClass
    public static void setup() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(gcmXPathFalsePath);
            xPathFalMsg = XMLParser.parse(fis);
            GetCurrentMessage msg = (GetCurrentMessage)xPathFalMsg.get_message();
            xPathFalse = msg.getTopic();
            fis.close();
            fis = new FileInputStream(gcmXPathMulPath);
            xPathMulMsg = XMLParser.parse(fis);
            msg = (GetCurrentMessage)xPathMulMsg.get_message();
            xPathMultipleHits = msg.getTopic();
            fis.close();
            fis = new FileInputStream(gcmXPathSinPath);
            xPathSinMsg = XMLParser.parse(fis);
            msg = (GetCurrentMessage)xPathSinMsg.get_message();
            xPathSingleHit = msg.getTopic();
            fis.close();
            fis = new FileInputStream(gcmIllegalDialectPath);
            illExprDiaMsg = XMLParser.parse(fis);
            msg = (GetCurrentMessage)illExprDiaMsg.get_message();
            illegalExpressionDialect = msg.getTopic();
            fis.close();
            fis = new FileInputStream(topicNamespacePath);
            topNSMsg = XMLParser.parse(fis);
            JAXBElement<TopicNamespaceType> ns = (JAXBElement)topNSMsg.get_message();
            topicNamespace = ns.getValue();
            fis.close();
            fis = new FileInputStream(topicSetPath);
            topSetMsg = XMLParser.parse(fis);
            JAXBElement<TopicSetType> ts = (JAXBElement)topSetMsg.get_message();
            topicSet = ts.getValue();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
    }

    // TODO Very few test cases are covered. Should cover more!

    @Test
    public void disassembleNamespaceContext() {
        NamespaceContext nsc = xPathMulMsg.getNamespaceContext();
        System.out.println("\n\nNamespace Context:\n\n" + nsc + "\n");
    }

    @Test
    public void disassembleTopicSetToOutput() {
        System.out.println("Topic set any size: " + topicSet.getAny().size());
        for (Object o : topicSet.getAny()) {
            System.out.println("Topic child class: " + o.getClass().toString());
            ElementNSImpl elementNS = (ElementNSImpl)o;
            System.out.println("\t" + elementNS.toString());
            System.out.println("\tNamespace URI: " + elementNS.getNamespaceURI());
            System.out.println("\tBase URI:" + elementNS.getBaseURI());
            System.out.println("\tLocal name: " + elementNS.getLocalName());
            System.out.println("\tPrefix: " + elementNS.getPrefix());
            System.out.println("\tTypeName: " + elementNS.getTypeName());
            System.out.println("\tTypeNamespaceUri: " + elementNS.getTypeNamespace());
            System.out.println("\tAttribute length: " + elementNS.getAttributes().getLength());
            System.out.println("\tAttributes:");
            NamedNodeMap attributes = elementNS.getAttributes();
            String indent = "\t\t";
            while (attributes != null) {
                Node node;
                for (int i = 0; i < attributes.getLength(); i++) {
                    node = attributes.item(i);
                    System.out.println();
                    System.out.println(indent + "Prefix: " + node.getPrefix());
                    System.out.println(indent + "Base URI: " + node.getBaseURI());
                    System.out.println(indent + "Namespace URI" + node.getNamespaceURI());
                    System.out.println(indent + "Local name: " + node.getLocalName());
                    System.out.println(indent + "Text content: " + node.getTextContent());
                }
                attributes = null;
            }
        }
    }

    @Test
    public void testIsTopicPermittedInNamespace() throws Exception {
        // TODO testcode
    }

    @Test
    public void testGetIntersectionNull() throws Exception{
        Assert.assertNull("Intersection was not empty!", TopicValidator.getIntersection(xPathFalse, topicSet, xPathFalMsg.getNamespaceContext()));
    }

    @Test
    public void testGetIntersectionOne() throws Exception{
        // Do calculation
        TopicSetType ret = TopicValidator.getIntersection(xPathSingleHit, topicSet, xPathSinMsg.getNamespaceContext());
        // Convert to more easily readable format
        List<List<QName>> retAsQNameList = TopicUtils.topicSetToQNameList(ret, false);
        Assert.assertNotNull("TopicValidator returned null!", ret);
        Assert.assertEquals("Topic evaluation returned wrong number of topics!", 1, retAsQNameList.size());

        // Check for correctness
        QName expectedName = new QName(testNamespace, testChildTopic);
        Assert.assertEquals("Topic selected had unexpected name!", expectedName, retAsQNameList.get(0));

        // Write to file, so it is possible to see actual content of returned set
        JAXBElement e = new JAXBElement<>(new QName("http://docs.oasis-open.org/wsn/t-1", "TopicSet"), TopicSetType.class, ret);
        XMLParser.writeObjectToStream(e, new FileOutputStream(OUTGcmXPathSinPath));
    }

    @Test
    public void testGetIntersectionTwo() throws Exception{
        // Do calculation
        TopicSetType ret = TopicValidator.getIntersection(xPathMultipleHits, topicSet, xPathMulMsg.getNamespaceContext());
        // Convert to more easily readable format
        List<List<QName>> retAsQNameList = TopicUtils.topicSetToQNameList(ret, false);
        Assert.assertNotNull("TopicValidator returned null!", ret);
        Assert.assertEquals("Topic evaluation returned wrong number of topics!", 3, retAsQNameList.size());

        // Check for correct content
        QName root1 = new QName(testNamespace, testRootTopic1);
        QName child = new QName(testNamespace, testChildTopic);
        QName root2 = new QName(testNamespace, testRootTopic2);
        Assert.assertTrue("Returned list did not contain root_topic1!", retAsQNameList.contains(root1));
        Assert.assertTrue("Returned list did not contain root_topic2!", retAsQNameList.contains(root2));
        Assert.assertTrue("Returned list did not contain child_topic!", retAsQNameList.contains(child));

        JAXBElement e = new JAXBElement<>(new QName("http://docs.oasis-open.org/wsn/t-1", "TopicSet"), TopicSetType.class, ret);
        XMLParser.writeObjectToStream(e, new FileOutputStream(OUTGcmXPathMulPath));
    }

    @Test(expected = TopicExpressionDialectUnknownFault.class)
    public void testIllegalExpressionDialectNamespace() throws Exception {
        TopicValidator.isExpressionPermittedInNamespace(illegalExpressionDialect, topicNamespace);
    }

    @Test(expected = TopicExpressionDialectUnknownFault.class)
    public void testIllegalExpressionDialectIntersection() throws Exception {
        TopicValidator.getIntersection(illegalExpressionDialect, topicSet, illExprDiaMsg.getNamespaceContext());
    }

    @Test(expected = TopicExpressionDialectUnknownFault.class)
    public void testIllegalExpressionDialectTopicExpression() throws Exception {
        TopicValidator.evaluateTopicWithExpression(illegalExpressionDialect, topicNamespace.getTopic().get(0));
    }

    @Test
    public void testEvaluateTopicWithExpressionLegal() throws Exception {
        // Child of first root topic should evaluate to true
        TopicType topic = topicNamespace.getTopic().get(0).getTopic().get(0);
        Assert.assertTrue("XPath evaluated topic falsely to false", TopicValidator.evaluateTopicWithExpression(xPathSingleHit, topic));
    }

    @Test
    public void testEvaluateTopicWithExpressionIllegal() throws Exception {
        TopicType topic = topicNamespace.getTopic().get(0).getTopic().get(0);
        Assert.assertFalse("XPath evaluated topic falsely to true", TopicValidator.evaluateTopicWithExpression(xPathFalse, topic));
    }
}
