package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicType;

/**
 * Created by Inge on 10.03.14.
 */
public class XPathEvaluator extends AbstractTopicEvaluator {
    @Override
    public String getDialectURIAsString() {
        return "http://www.w3.org/TR/1999/REC-xpath-19991116";
    }

    @Override
    public boolean evaluateTopicWithExpression(TopicExpressionType topicExpressionType, TopicType topicType)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // TODO
         /* Something for inspiration:
         
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression xPathExpression = null;
        try {
            xPathExpression = xPath.compile(xExpression);
        } catch (XPathExpressionException e) {
            // TODO Respond to malformed XPATH expression
            e.printStackTrace();
        }
        try {
            Object evaluated = xPathExpression.evaluate(topicType, XPathConstants.BOOLEAN);
            // TODO Test for correctness
            return XPathConstants.BOOLEAN.equals(evaluated);
        } catch (XPathExpressionException e) {
            // TODO respond to that we are unable to evaluate the expression correctly
            e.printStackTrace();
        }
    */
        return false;
    }
}
