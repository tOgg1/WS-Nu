package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicNamespaceType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;

/**
 * Created by tormod on 3/3/14.
 */
public class TopicValidator {

    /**
     * Should never be instantiated
     */
    private TopicValidator() {
    }


    /**
     * Evaluate the Topic a TopicExpression is describing is permitted in TopicNamespace given. Described in
     * [Web Services Topics 1.3 (WS-Topics) OASIS Standard, 1 October 2006, section 8.5]
     *
     * @param expression The expression to examine
     * @param namespace  The namespace under consideration
     * @return <code>true</code> if allowed. <code>false</code> if not allowed.
     */
    public static boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // TODO
        return false;
    }

    /**
     * Gets the intersection between the Topics selected by an TopicExpression and a given TopicSet. Described in
     * [Web Services Topics 1.3 (WS-Topics) OASIS Standard, 1 October 2006, section 8.5]
     *
     * @param expression the expression to examine
     * @param topicSet   the TopicSet to evaluate against
     * @return The TopicSet given by the intersection. <code>null</code> if no elements are in the intersection.
     */
    public static TopicSetType getIntersection(TopicExpressionType expression, TopicSetType topicSet)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // TODO
        return null;
    }

    /**
     * Evaluates if a given TopicExpression fits a given Topic. This hides complexity with TopicExpressionDialects.
     *
     * @param expression The expression to examine
     * @param topic      The Topic to evaluate against.
     * @return <code>true</code> if expression covers Topic. <code>false</code> otherwise.
     */
    public static boolean evaluateTopicExpressionAgainstTopic(TopicExpressionType expression, TopicType topic)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // TODO write
        return false;
    }

}
