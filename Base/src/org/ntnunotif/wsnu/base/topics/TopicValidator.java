package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.t_1.TopicType;

import javax.xml.namespace.QName;
import javax.xml.xpath.*;

/**
 * Created by tormod on 3/3/14.
 */
public class TopicValidator {

    /**
     * Should never be instantiated
     */
    private TopicValidator() {}

    public static boolean validateXPath(String xExpression, TopicType topicType) {
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
        return false;
    }

}
