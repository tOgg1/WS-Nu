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
import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
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
import java.util.ArrayList;
import java.util.List;

/**
 * <code>TopicValidatorTest</code> tests the <code>TopicValidator</code>. It is dependent on <code>XMLParser</code>
 * Created by Inge on 06.03.14.
 */
public class TopicValidatorTest {
    private static final String gcmXPathMulPath = "Base/testres/topic_gcm_xpath_boolean_multiple_test.xml";
    private static final String gcmXPathSinPath = "Base/testres/topic_gcm_xpath_boolean_single_test.xml";
    private static final String gcmXPathFalsePath = "Base/testres/topic_gcm_xpath_false.xml";
    private static final String gcmIllegalDialectPath = "Base/testres/topic_gcm_illegal_dialect_test.xml";
    private static final String topicNamespacePath = "Base/testres/topic_namespace_test.xml";
    private static final String topicSetPath = "Base/testres/topic_set_test.xml";
    private static final String OUTGcmXPathMulPath = "Base/testres/out_topic_gcm_xpath_boolean_multiple_test.xml";
    private static final String OUTGcmXPathSinPath = "Base/testres/out_topic_gcm_xpath_boolean_single_test.xml";

    // locations of simple, concrete and full expressions
    private static final String simpleLegalLocation = "Base/testres/topic_gcm_simple_legal_test.xml";
    private static final String simpleIllegalLocation = "Base/testres/topic_gcm_simple_illegal_test.xml";

    private static final String concreteLegalLocation = "Base/testres/topic_gcm_concrete_legal_test.xml";
    private static final String concreteIllegalLocation = "Base/testres/topic_gcm_concrete_illegal_test.xml";

    private static final String fullLegalSingleLocation = "Base/testres/topic_gcm_full_legal_single_test.xml";
    private static final String fullLegalMultipleLocation = "Base/testres/topic_gcm_full_legal_multiple_test.xml";
    private static final String fullIllegalLocation = "Base/testres/topic_gcm_full_illegal_test.xml";

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

    // Internal messages for simple, concrete and full expressions
    private static InternalMessage simpleLegalMsg;
    private static InternalMessage simpleIllegalMsg;

    private static InternalMessage concreteLegalMsg;
    private static InternalMessage concreteIllegalMsg;

    private static InternalMessage fullLegalSingleMsg;
    private static InternalMessage fullLegalMultipleMsg;
    private static InternalMessage fullIllegalMsg;


    private static final String testNamespace = "http://ws-nu.org/testTopicSpace1";
    private static final String testRootTopic1 = "root_topic1";
    private static final String testChildTopicLocalName = "child_topic";
    private static final String testRootTopic2 = "root_topic2";

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

            // Simple load
            fis = new FileInputStream(simpleLegalLocation);
            simpleLegalMsg = XMLParser.parse(fis);
            fis.close();
            fis = new FileInputStream(simpleIllegalLocation);
            simpleIllegalMsg = XMLParser.parse(fis);
            fis.close();

            // concrete load
            fis = new FileInputStream(concreteLegalLocation);
            concreteLegalMsg = XMLParser.parse(fis);
            fis.close();
            fis = new FileInputStream(concreteIllegalLocation);
            concreteIllegalMsg = XMLParser.parse(fis);
            fis.close();

            // full load
            fis = new FileInputStream(fullLegalSingleLocation);
            fullLegalSingleMsg = XMLParser.parse(fis);
            fis.close();
            fis = new FileInputStream(fullLegalMultipleLocation);
            fullLegalMultipleMsg = XMLParser.parse(fis);
            fis.close();
            fis = new FileInputStream(fullIllegalLocation);
            fullIllegalMsg = XMLParser.parse(fis);
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
    public void testSimpleLegalCase() throws Exception{
        NamespaceContext namespaceContext = simpleLegalMsg.getRequestInformation().getNamespaceContext();
        TopicExpressionType topicExpression = ((GetCurrentMessage)simpleLegalMsg.getMessage()).getTopic();

        Assert.assertTrue("Simple legal not accepted", TopicValidator.isLegalExpression(topicExpression, namespaceContext));
    }

    @Test(expected = InvalidTopicExpressionFault.class)
    public void testSimpleIllegalCase() throws Exception{
        NamespaceContext namespaceContext = simpleIllegalMsg.getRequestInformation().getNamespaceContext();
        TopicExpressionType topicExpression = ((GetCurrentMessage)simpleIllegalMsg.getMessage()).getTopic();

        TopicValidator.isLegalExpression(topicExpression, namespaceContext);
    }

    @Test
    public void testConcreteLegalCase() throws Exception{
        NamespaceContext namespaceContext = concreteLegalMsg.getRequestInformation().getNamespaceContext();
        TopicExpressionType topicExpression = ((GetCurrentMessage)concreteLegalMsg.getMessage()).getTopic();

        Assert.assertTrue("Concrete legal not accepted", TopicValidator.isLegalExpression(topicExpression, namespaceContext));
    }

    @Test(expected = InvalidTopicExpressionFault.class)
    public void testConcreteIllegalCase() throws Exception{
        NamespaceContext namespaceContext = concreteIllegalMsg.getRequestInformation().getNamespaceContext();
        TopicExpressionType topicExpression = ((GetCurrentMessage)concreteIllegalMsg.getMessage()).getTopic();

        TopicValidator.isLegalExpression(topicExpression, namespaceContext);
    }

    @Test
     public void testFullLegalSingleCase() throws Exception{
        NamespaceContext namespaceContext = fullLegalSingleMsg.getRequestInformation().getNamespaceContext();
        TopicExpressionType topicExpression = ((GetCurrentMessage)fullLegalSingleMsg.getMessage()).getTopic();

        Assert.assertTrue("Full, single legal not accepted", TopicValidator.isLegalExpression(topicExpression, namespaceContext));
    }

    @Test
    public void testFullLegalMultipleCase() throws Exception{
        NamespaceContext namespaceContext = fullLegalMultipleMsg.getRequestInformation().getNamespaceContext();
        TopicExpressionType topicExpression = ((GetCurrentMessage)fullLegalMultipleMsg.getMessage()).getTopic();

        Assert.assertTrue("full, multiple legal not accepted", TopicValidator.isLegalExpression(topicExpression, namespaceContext));
    }

    @Test(expected = InvalidTopicExpressionFault.class)
    public void testFullIllegalCase() throws Exception{
        NamespaceContext namespaceContext = fullIllegalMsg.getRequestInformation().getNamespaceContext();
        TopicExpressionType topicExpression = ((GetCurrentMessage)fullIllegalMsg.getMessage()).getTopic();

        TopicValidator.isLegalExpression(topicExpression, namespaceContext);
    }

    @Test
    public void testExpressionsToQNameList() throws Exception {

        // Simple
        List<QName> qNameList = TopicValidator.evaluateTopicExpressionToQName(
                ((GetCurrentMessage)simpleLegalMsg.getMessage()).getTopic(),
                simpleLegalMsg.getRequestInformation().getNamespaceContext()
        );

        Assert.assertNotNull("Simple was evaluated to null", qNameList);
        Assert.assertEquals("Simple was wrong length", 1, qNameList.size());

        // Concrete
        qNameList = TopicValidator.evaluateTopicExpressionToQName(
                ((GetCurrentMessage)concreteLegalMsg.getMessage()).getTopic(),
                concreteLegalMsg.getRequestInformation().getNamespaceContext()
        );

        Assert.assertNotNull("Concrete was evaluated to null", qNameList);
        Assert.assertEquals("Concrete was wrong length", 2, qNameList.size());

        // full
        qNameList = TopicValidator.evaluateTopicExpressionToQName(
                ((GetCurrentMessage)fullLegalSingleMsg.getMessage()).getTopic(),
                fullLegalSingleMsg.getRequestInformation().getNamespaceContext()
        );

        Assert.assertNotNull("Full was evaluated to null", qNameList);
        Assert.assertEquals("Full was wrong length", 2, qNameList.size());
    }

    @Test(expected = MultipleTopicsSpecifiedFault.class)
    public void testExpressionsMultipleFull() throws Exception{
        TopicValidator.evaluateTopicExpressionToQName(
                ((GetCurrentMessage)fullLegalMultipleMsg.getMessage()).getTopic(),
                fullLegalMultipleMsg.getRequestInformation().getNamespaceContext()
        );
    }

    @Test
    public void disassembleNamespaceContext() {
        NamespaceContext nsc = xPathMulMsg.getRequestInformation().getNamespaceContext();
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
    public void testGetIntersectionNull() throws Exception{
        Assert.assertNull("Intersection was not empty!", TopicValidator.getIntersection(xPathFalse, topicSet, xPathFalMsg.getRequestInformation().getNamespaceContext()));
    }

    @Test
    public void testGetIntersectionOne() throws Exception{
        // Do calculation
        TopicSetType ret = TopicValidator.getIntersection(xPathSingleHit, topicSet, xPathSinMsg.getRequestInformation().getNamespaceContext());
        // Convert to more easily readable format
        List<List<QName>> retAsQNameList = TopicUtils.topicSetToQNameList(ret, false);
        Assert.assertNotNull("TopicValidator returned null!", ret);
        Assert.assertEquals("Topic evaluation returned wrong number of topics!", 1, retAsQNameList.size());

        // Check for correctness
        List<QName> expectedName = new ArrayList<>();
        expectedName.add(new QName(testNamespace, testRootTopic1));
        expectedName.add(new QName(testChildTopicLocalName));
        Assert.assertEquals("Topic selected had unexpected name!", expectedName, retAsQNameList.get(0));

        // Write to file, so it is possible to see actual content of returned set
        JAXBElement e = new JAXBElement<>(new QName("http://docs.oasis-open.org/wsn/t-1", "TopicSet"), TopicSetType.class, ret);
        XMLParser.writeObjectToStream(e, new FileOutputStream(OUTGcmXPathSinPath));
    }

    @Test
    public void testGetIntersectionTwo() throws Exception{
        // Do calculation
        TopicSetType ret = TopicValidator.getIntersection(xPathMultipleHits, topicSet, xPathMulMsg.getRequestInformation().getNamespaceContext());
        // Convert to more easily readable format
        List<List<QName>> retAsQNameList = TopicUtils.topicSetToQNameList(ret, false);
        Assert.assertNotNull("TopicValidator returned null!", ret);
        Assert.assertEquals("Topic evaluation returned wrong number of topics!", 3, retAsQNameList.size());

        // Check for correct content
        List<QName> root1 = new ArrayList<>();
        root1.add(new QName(testNamespace, testRootTopic1));
        List<QName> child = new ArrayList<>();
        child.add(new QName(testNamespace, testRootTopic1));
        child.add(new QName(testChildTopicLocalName));
        List<QName> root2 = new ArrayList<>();
        root2.add(new QName(testNamespace, testRootTopic2));

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
        TopicValidator.getIntersection(illegalExpressionDialect, topicSet, illExprDiaMsg.getRequestInformation().getNamespaceContext());
    }

    @Test(expected = TopicExpressionDialectUnknownFault.class)
    public void testIllegalExpressionDialectTopicExpression() throws Exception {
        TopicValidator.evaluateTopicWithExpression(illegalExpressionDialect, topicNamespace.getTopic().get(0));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEvaluateTopicWithExpressionLegal() throws Exception {
        // Child of first root topic should evaluate to true
        TopicType topic = topicNamespace.getTopic().get(0).getTopic().get(0);
        Assert.assertTrue("XPath evaluated topic falsely to false", TopicValidator.evaluateTopicWithExpression(xPathSingleHit, topic));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEvaluateTopicWithExpressionIllegal() throws Exception {
        TopicType topic = topicNamespace.getTopic().get(0).getTopic().get(0);
        Assert.assertFalse("XPath evaluated topic falsely to true", TopicValidator.evaluateTopicWithExpression(xPathFalse, topic));
    }

    @Test
    public void testIsTopicPermittedInNamespace() throws Exception {
        // TODO testcode
    }
}
