package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicNamespaceType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * Created by Inge on 21.03.2014.
 */
public class SimpleEvaluator implements TopicExpressionEvaluatorInterface {
    /**
     * The dialect this evaluator supports
     */
    public static final String dialectURI = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";

    @Override
    public String getDialectURIAsString() {
        return dialectURI;
    }

    @Override
    public boolean evaluateTopicWithExpression(TopicExpressionType topicExpressionType, TopicType topicType) throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        return false;
    }

    @Override
    public TopicSetType getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType, NamespaceContext namespaceContext) throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        return null;
    }

    @Override
    public boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace) throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        return false;
    }

    @Override
    public QName evaluateTopicExpressionToQName(TopicExpressionType topicExpressionType) throws UnsupportedOperationException, InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault {
        return null;
    }
}
