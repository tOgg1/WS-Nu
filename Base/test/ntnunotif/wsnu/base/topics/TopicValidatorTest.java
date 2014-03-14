package ntnunotif.wsnu.base.topics;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import com.sun.org.apache.xerces.internal.util.NamespaceContextWrapper;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.InternalMessage;
import org.ntnunotif.wsnu.base.net.XMLParser;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
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

    @BeforeClass
    public static void setup() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(gcmXPathFalsePath);
            xPathFalMsg = XMLParser.parse(fis);
            GetCurrentMessage msg = (GetCurrentMessage)xPathFalMsg.getMessage();
            xPathFalse = msg.getTopic();
            fis.close();
            fis = new FileInputStream(gcmXPathMulPath);
            xPathMulMsg = XMLParser.parse(fis);
            msg = (GetCurrentMessage)xPathMulMsg.getMessage();
            xPathMultipleHits = msg.getTopic();
            fis.close();
            fis = new FileInputStream(gcmXPathSinPath);
            xPathSinMsg = XMLParser.parse(fis);
            msg = (GetCurrentMessage)xPathSinMsg.getMessage();
            xPathSingleHit = msg.getTopic();
            fis.close();
            fis = new FileInputStream(gcmIllegalDialectPath);
            illExprDiaMsg = XMLParser.parse(fis);
            msg = (GetCurrentMessage)illExprDiaMsg.getMessage();
            illegalExpressionDialect = msg.getTopic();
            fis.close();
            fis = new FileInputStream(topicNamespacePath);
            topNSMsg = XMLParser.parse(fis);
            JAXBElement<TopicNamespaceType> ns = (JAXBElement)topNSMsg.getMessage();
            topicNamespace = ns.getValue();
            fis.close();
            fis = new FileInputStream(topicSetPath);
            topSetMsg = XMLParser.parse(fis);
            JAXBElement<TopicSetType> ts = (JAXBElement)topSetMsg.getMessage();
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
        List<TopicType> ret = TopicValidator.getIntersection(xPathSingleHit, topicSet, xPathSinMsg.getNamespaceContext());
        Assert.assertNotNull("TopicValidator returned null!", ret);
        Assert.assertEquals("Topic evaluation returned wrong number of topics!", 1 , ret.size());
    }

    @Test
    public void testGetIntersectionTwo() throws Exception{
        List<TopicType> ret = TopicValidator.getIntersection(xPathMultipleHits, topicSet, xPathMulMsg.getNamespaceContext());
        Assert.assertNotNull("TopicValidator returned null!", ret);
        Assert.assertEquals("Topic evaluation returned wrong number of topics!", 3, ret.size());
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
        // TODO testcode
    }

    @Test
    public void testEvaluateTopicWithExpressionIllegal() throws Exception {
        TopicType topic = topicNamespace.getTopic().get(0).getTopic().get(0);
        Assert.assertFalse("XPath evaluated topic falsely to true", TopicValidator.evaluateTopicWithExpression(xPathFalse, topic));
        // TODO testcode
    }
}
