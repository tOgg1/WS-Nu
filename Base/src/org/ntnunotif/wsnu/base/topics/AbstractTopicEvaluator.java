package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;

/**
 * Created by Inge on 07.03.14.
 */
public abstract class AbstractTopicEvaluator implements TopicExpressionEvaluatorInterface {
    @Override
    public TopicSetType evaluateTopicSetWithExpression(TopicSetType topicSetType, TopicExpressionType topicExpressionType) {
        // TODO This should be possible to handle for an abstract class.
        return null;
    }
}
