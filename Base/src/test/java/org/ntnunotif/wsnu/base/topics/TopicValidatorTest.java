package org.ntnunotif.wsnu.base.topics;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.ObjectFactory;
import org.oasis_open.docs.wsn.t_1.TopicNamespaceType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>TopicValidatorTest</code> tests the <code>TopicValidator</code>. It is dependent on <code>XMLParser</code>
 * Created by Inge on 06.03.14.
 */
public class TopicValidatorTest {
    private static final String gcmXPathMulPathRes = "/topic_gcm_xpath_boolean_multiple_test.xml";
    private static final String gcmXPathSinPathRes = "/topic_gcm_xpath_boolean_single_test.xml";
    private static final String gcmXPathFalsePathRes = "/topic_gcm_xpath_false.xml";
    private static final String gcmIllegalDialectPathRes = "/topic_gcm_illegal_dialect_test.xml";
    private static final String topicNamespacePathRes = "/topic_namespace_test.xml";
    private static final String topicSetPathRes = "/topic_set_test.xml";
    private static final String OUTGcmXPathMulPathRes = "/out_topic_gcm_xpath_boolean_multiple_test.xml";
    private static final String OUTGcmXPathSinPathRes = "/out_topic_gcm_xpath_boolean_single_test.xml";

    // locations of simple, concrete and full expressions
    private static final String simpleLegalLocationRes = "/topic_gcm_simple_legal_test.xml";
    private static final String simpleIllegalLocationRes = "/topic_gcm_simple_illegal_test.xml";

    private static final String concreteLegalLocationRes = "/topic_gcm_concrete_legal_test.xml";
    private static final String concreteIllegalLocationRes = "/topic_gcm_concrete_illegal_test.xml";

    private static final String fullLegalSingleLocationRes = "/topic_gcm_full_legal_single_test.xml";
    private static final String fullLegalMultipleLocationRes = "/topic_gcm_full_legal_multiple_test.xml";
    private static final String fullIllegalLocationRes = "/topic_gcm_full_illegal_test.xml";

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
    public static void setUp() {
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        InputStream fis = null;
        try {
            fis = TopicValidatorTest.class.getResourceAsStream(gcmXPathFalsePathRes);
            xPathFalMsg = XMLParser.parse(fis);
            GetCurrentMessage msg = (GetCurrentMessage)xPathFalMsg.getMessage();
            xPathFalse = msg.getTopic();

            fis = TopicValidatorTest.class.getResourceAsStream(gcmXPathMulPathRes);
            xPathMulMsg = XMLParser.parse(fis);
            msg = (GetCurrentMessage)xPathMulMsg.getMessage();
            xPathMultipleHits = msg.getTopic();

            fis = TopicValidatorTest.class.getResourceAsStream(gcmXPathSinPathRes);
            xPathSinMsg = XMLParser.parse(fis);
            msg = (GetCurrentMessage)xPathSinMsg.getMessage();
            xPathSingleHit = msg.getTopic();

            fis = TopicValidatorTest.class.getResourceAsStream(gcmIllegalDialectPathRes);
            illExprDiaMsg = XMLParser.parse(fis);
            msg = (GetCurrentMessage)illExprDiaMsg.getMessage();
            illegalExpressionDialect = msg.getTopic();

            fis = TopicValidatorTest.class.getResourceAsStream(topicNamespacePathRes);
            topNSMsg = XMLParser.parse(fis);
            JAXBElement<TopicNamespaceType> ns = (JAXBElement)topNSMsg.getMessage();
            topicNamespace = ns.getValue();

            fis = TopicValidatorTest.class.getResourceAsStream(topicSetPathRes);
            topSetMsg = XMLParser.parse(fis);
            JAXBElement<TopicSetType> ts = (JAXBElement)topSetMsg.getMessage();
            topicSet = ts.getValue();

            // Simple load
            fis = TopicValidatorTest.class.getResourceAsStream(simpleLegalLocationRes);
            simpleLegalMsg = XMLParser.parse(fis);

            fis = TopicValidatorTest.class.getResourceAsStream(simpleIllegalLocationRes);
            simpleIllegalMsg = XMLParser.parse(fis);

            // concrete load
            fis = TopicValidatorTest.class.getResourceAsStream(concreteLegalLocationRes);
            concreteLegalMsg = XMLParser.parse(fis);

            fis = TopicValidatorTest.class.getResourceAsStream(concreteIllegalLocationRes);
            concreteIllegalMsg = XMLParser.parse(fis);

            // full load
            fis = TopicValidatorTest.class.getResourceAsStream(fullLegalSingleLocationRes);
            fullLegalSingleMsg = XMLParser.parse(fis);

            fis = TopicValidatorTest.class.getResourceAsStream(fullLegalMultipleLocationRes);
            fullLegalMultipleMsg = XMLParser.parse(fis);

            fis = TopicValidatorTest.class.getResourceAsStream(fullIllegalLocationRes);
            fullIllegalMsg = XMLParser.parse(fis);

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
    public void testGetIntersectionNull() throws Exception{
        Assert.assertNull("Intersection was not empty!", TopicValidator.getIntersection(xPathFalse, topicSet, xPathFalMsg.getRequestInformation().getNamespaceContext()));
    }

    @Test
    public void testGetIntersectionOne() throws Exception{
        // Do calculation
        TopicSetType ret = TopicValidator.getIntersection(xPathSingleHit, topicSet, xPathSinMsg.getRequestInformation().getNamespaceContext());
        // Convert to more easily readable format
        List<List<QName>> retAsQNameList = TopicUtils.topicSetToQNameList(ret, true);
        Assert.assertNotNull("TopicValidator returned null!", ret);
        System.out.println(retAsQNameList);
        Assert.assertEquals("Topic evaluation returned wrong number of topics!", 1, retAsQNameList.size());

        // Check for correctness
        List<QName> expectedName = new ArrayList<>();
        expectedName.add(new QName(testNamespace, testRootTopic1));
        expectedName.add(new QName(testChildTopicLocalName));
        Assert.assertEquals("Topic selected had unexpected name!", expectedName, retAsQNameList.get(0));

        // Write to file, so it is possible to see actual content of returned set
        JAXBElement e = new JAXBElement<>(new QName("http://docs.oasis-open.org/wsn/t-1", "TopicSet"), TopicSetType.class, ret);
        XMLParser.writeObjectToStream(e, new FileOutputStream(getClass().getResource(OUTGcmXPathSinPathRes).getFile()));
    }

    @Test
    public void testGetIntersectionTwo() throws Exception{
        // Do calculation
        TopicSetType ret = TopicValidator.getIntersection(xPathMultipleHits, topicSet, xPathMulMsg.getRequestInformation().getNamespaceContext());
        // Convert to more easily readable format
        List<List<QName>> retAsQNameList = TopicUtils.topicSetToQNameList(ret, true);
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

        ObjectFactory factory = new ObjectFactory();
        JAXBElement e = factory.createTopicSet(ret);
        XMLParser.writeObjectToStream(e, new FileOutputStream(getClass().getResource(OUTGcmXPathMulPathRes).getFile()));
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
