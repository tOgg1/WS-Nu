package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicNamespaceType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 07.03.14.
 */
public abstract class AbstractTopicEvaluator implements TopicExpressionEvaluatorInterface {

    @Override
    public List<TopicType> getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType, NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        List<TopicType> retList = new ArrayList<TopicType>();
        for (TopicType tt: TopicUtils.topicSetToTopicTypeList(topicSetType)) {
            if (evaluateTopicWithExpression(topicExpressionType, tt))
                retList.add(tt);
        }
        return retList.size() == 0 ? null : retList;
    }

    @Override
    public boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // TODO
        return false;
    }
}
