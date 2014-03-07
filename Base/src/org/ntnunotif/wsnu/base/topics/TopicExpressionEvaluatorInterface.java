package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;

/**
 * A <code>TopicExpressionEvaluatorInterface</code> is an interface that should be implemented by objects that wishes
 * to compare a {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} with a given dialect to
 * {@link org.oasis_open.docs.wsn.t_1.TopicType}s.
 *
 * @author Inge Edward Halsaunet
 * Created by Inge on 07.03.14.
 */
public interface TopicExpressionEvaluatorInterface {
    /**
     * Gets the dialect this <code>TopicExpressionEvaluatorInterface</code> can handle.
     * @return
     */
    public String getDialectURIAsString();

    /**
     * Evaluates if the {@link org.oasis_open.docs.wsn.t_1.TopicType} is covered by the
     * {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}.
     *
     * @param topicType the Topic to evaluate
     * @param topicExpressionType the expression to evaluate with
     * @return <code>true</code> if topic is covered. <code>false</code> otherwise.
     */
    public boolean evaluateTopicWithExpression(TopicType topicType, TopicExpressionType topicExpressionType);

    /**
     * Evaluates all {@link org.oasis_open.docs.wsn.t_1.TopicType}s contained in the
     * {@link org.oasis_open.docs.wsn.t_1.TopicSetType} with the {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}.
     *
     * @param topicSetType the TopicSet to evaluate.
     * @param topicExpressionType the expression to evaluate with.
     * @return the set of all Topics in the queried set covered by the expression-
     */
    public TopicSetType evaluateTopicSetWithExpression(TopicSetType topicSetType, TopicExpressionType topicExpressionType);

}
