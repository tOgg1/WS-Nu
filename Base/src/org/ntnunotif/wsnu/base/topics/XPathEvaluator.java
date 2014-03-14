package org.ntnunotif.wsnu.base.topics;

import com.sun.org.apache.xerces.internal.dom.DocumentFragmentImpl;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import com.sun.org.apache.xpath.internal.NodeSet;
import com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.internal.ws.util.xml.NodeListIterator;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sun.plugin.dom.core.Document;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Inge on 10.03.14.
 */
public class XPathEvaluator extends AbstractTopicEvaluator {

    /**
     * The dialect this evaluator supports
     */
    public static final String dialectURI = "http://www.w3.org/TR/1999/REC-xpath-19991116";

    @Override
    public String getDialectURIAsString() {
        return dialectURI;
    }

    @Override
    public boolean evaluateTopicWithExpression(TopicExpressionType topicExpressionType, TopicType topicType)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        if (!topicExpressionType.getDialect().equals(dialectURI)) {
            // TODO Fill in exception
            throw new TopicExpressionDialectUnknownFault();
        }
        String expression = null;
        for (Object o : topicExpressionType.getContent()) {
            if (o instanceof String) {
                if (expression != null) {
                    // TODO respond to multiple strings in expression
                }
                expression = (String) o;
            }
        }
        if (expression == null) {
            // TODO Find exception for no expression in tag
        }
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression xPathExpression = null;
        try {
            xPathExpression = xPath.compile(expression);
        } catch (XPathExpressionException e) {
            // TODO fill in exception
            throw new InvalidTopicExpressionFault();
        }
        System.out.println(xPathExpression);
        /*
        try {
            //Object evaluated = xPathExpression.evaluate(, XPathConstants.NODESET);
            //System.out.println(evaluated);
            //return XPathConstants.BOOLEAN.equals(evaluated);
            return false;
        } catch (XPathExpressionException e) {
            // TODO fill in exception
            throw new InvalidTopicExpressionFault();
        }
        */
        return false;
    }

    @Override
    public TopicSetType getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType, NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        if (!topicExpressionType.getDialect().equals(dialectURI)) {
            // TODO Fill in exception
            throw new TopicExpressionDialectUnknownFault();
        }

        // Find expression string
        String expression = null;
        for (Object o : topicExpressionType.getContent()) {
            if (o instanceof String) {
                if (expression != null) {
                    // TODO respond to multiple strings in expression
                }
                expression = (String) o;
            }
        }
        if (expression == null) {
            // TODO Find exception for no expression in tag
        }

        // Build XPath environment
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        // This should make expression solution more correct
        xPath.setNamespaceContext(namespaceContext);
        XPathExpression xPathExpression;
        try {
            xPathExpression = xPath.compile(expression);
        } catch (XPathExpressionException e) {
            // TODO fill in exception, not an legal XPath expression
            throw new InvalidTopicExpressionFault();
        }


        // For every object in topicset, try to evaluate it against expression and store result.
        int returnCount = 0;
        TopicSetType returnSet = new TopicSetType();
        for (Object o: topicSetType.getAny()) {
            try {
                NodeList nodeList = (NodeList) xPathExpression.evaluate(o, XPathConstants.NODESET);
                // If result contained topics, add them to return topic set.
                returnCount += nodeList.getLength();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    returnSet.getAny().add(node);
                }
            } catch (XPathExpressionException e) {
                // TODO fill in exception
                throw new InvalidTopicExpressionFault();
            }
        }
        if (returnCount == 0)
            return null;
        return returnSet;
    }
}
