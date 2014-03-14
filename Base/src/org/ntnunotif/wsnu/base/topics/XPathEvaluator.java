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
    public List<TopicType> getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType, NamespaceContext namespaceContext)
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
        Map<QName,String> other = topicExpressionType.getOtherAttributes();
        System.out.println("\nOther attributes: \n");
        for (QName n: other.keySet()) {
            System.out.println(n.toString() + "\t:\t" + other.get(n));
        }
        if (expression == null) {
            // TODO Find exception for no expression in tag
        }
        System.out.println("\nEXPRESSION: \n" + expression + "\n");

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        xPath.setNamespaceContext(namespaceContext);


        for (Object o: topicSetType.getAny()) {
            try {
                NodeList nodeList = (NodeList) xPath.evaluate(expression, o, XPathConstants.NODESET);
                System.out.println("Result length: " + nodeList.getLength());
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    System.out.println("\t" + node);
                    NamedNodeMap nodeMap = node.getAttributes();
                    for (int j = 0; j < nodeMap.getLength(); j++) {
                        System.out.println("\t\t" + (nodeMap.item(j).getNodeType() == Node.ATTRIBUTE_NODE ? "attribute: ": "other" )  + " \t" + nodeMap.item(j));
                    }
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
        throw new InvalidTopicExpressionFault("Finisher");

        /*
        XPathExpression xPathExpression;
        try {
            xPathExpression = xPath.compile(expression);
        } catch (XPathExpressionException e) {
            // TODO fill in exception
            throw new InvalidTopicExpressionFault();
        }
        for (Object o: topicSetType.getAny()) {
            try {
                System.out.println(o);
                System.out.println(o.getClass());
                Element elementNS = (ElementNSImpl) o;
                NamedNodeMap nodeMap = elementNS.getAttributes();
                System.out.println("Map length: " + nodeMap.getLength());
                for (int i = 0; i < nodeMap.getLength(); i++) {
                    System.out.println(nodeMap.item(i));
                }
                System.out.println(elementNS.getNamespaceURI());
                Object evaluated = xPathExpression.evaluate(o, XPathConstants.NODESET);
                NodeList s = (NodeList)evaluated;
                System.out.println(s.getLength());
                for (int i = 0; i < s.getLength(); i++) {
                    Node n = s.item(i);
                    System.out.println("\tHaha" + n);
                }
            } catch (XPathExpressionException e) {
                // TODO fill in exception
                throw new InvalidTopicExpressionFault();
            }
        }
        throw new InvalidTopicExpressionFault("Finisher");
        */
    }
}
