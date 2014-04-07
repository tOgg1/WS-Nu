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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tormod on 3/3/14.
 */
public class TopicValidator {
    private static Map<String, TopicExpressionEvaluatorInterface> topicExpressionEvaluators;

    /**
     * Instantiates the delegated evaluators.
     */
    static {
        topicExpressionEvaluators = new HashMap<String, TopicExpressionEvaluatorInterface>();
        TopicExpressionEvaluatorInterface evaluator = new XPathEvaluator();
        topicExpressionEvaluators.put(evaluator.getDialectURIAsString(), evaluator);
        evaluator = new SimpleEvaluator();
        topicExpressionEvaluators.put(evaluator.getDialectURIAsString(), evaluator);
        evaluator = new ConcreteEvaluator();
        topicExpressionEvaluators.put(evaluator.getDialectURIAsString(), evaluator);
        evaluator = new FullEvaluator();
        topicExpressionEvaluators.put(evaluator.getDialectURIAsString(), evaluator);
    }

    /**
     * Should never be instantiated
     */
    private TopicValidator() {
    }


    /**
     * Evaluate the <code>Topic</code> a {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} is describing is
     * permitted in {@link org.oasis_open.docs.wsn.t_1.TopicNamespaceType} given. Described in
     * [Web Services Topics 1.3 (WS-Topics) OASIS Standard, 1 October 2006, section 8.5]
     *
     * @param expression The expression to examine
     * @param namespace  The namespace under consideration
     * @return <code>true</code> if allowed. <code>false</code> if not allowed.
     * @throws org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault If the dialect of the
     *                                                                         {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was unknown
     * @throws org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault        If the dialect of the
     *                                                                         {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was inconsistent with actual expression.
     */
    public static boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // Delegating work
        String dialect = expression.getDialect();
        TopicExpressionEvaluatorInterface evaluator = topicExpressionEvaluators.get(dialect);
        // Check if we know this dialect
        if (evaluator == null) {
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "The TopicExpression dialect {" + dialect +
                    "} was unknown.");
            return false;
        }
        return evaluator.isExpressionPermittedInNamespace(expression, namespace);
    }

    /**
     * Gets the intersection between the <code>Topic</code>s selected by an
     * {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} and a given
     * {@link org.oasis_open.docs.wsn.t_1.TopicSetType}. Described in
     * [Web Services Topics 1.3 (WS-Topics) OASIS Standard, 1 October 2006, section 8.5]
     *
     * @param expression       the expression to examine
     * @param topicSet         the TopicSet to evaluate against
     * @param namespaceContext the {@link javax.xml.namespace.NamespaceContext} of the expression
     * @return The TopicSet given by the intersection. <code>null</code> if no elements are in the intersection.
     * @throws org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault If the dialect of the
     *                                                                         {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was unknown
     * @throws org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault        If the dialect of the
     *                                                                         {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was inconsistent with actual expression.
     */
    public static TopicSetType getIntersection(TopicExpressionType expression, TopicSetType topicSet,
                                               NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // Delegating work
        String dialect = expression.getDialect();
        TopicExpressionEvaluatorInterface evaluator = topicExpressionEvaluators.get(dialect);
        // Check if we know this dialect
        if (evaluator == null) {
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "The TopicExpression dialect {" + dialect +
                    "} was unknown.");
            return null;
        }
        return evaluator.getIntersection(expression, topicSet, namespaceContext);
    }

    /**
     * Evaluates if a given {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} fits a given
     * {@link org.oasis_open.docs.wsn.t_1.TopicType}. This hides complexity with <code>TopicExpressionDialect</code>s.
     *
     * @param expression The expression to examine
     * @param topic      The Topic to evaluate against.
     * @return <code>true</code> if expression covers Topic. <code>false</code> otherwise.
     * @throws org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault If the dialect of the
     *                                                                         {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was unknown
     * @throws org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault        If the dialect of the {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was inconsistent with actual expression.
     */
    public static boolean evaluateTopicWithExpression(TopicExpressionType expression, TopicType topic)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // Delegating work
        String dialect = expression.getDialect();
        TopicExpressionEvaluatorInterface evaluator = topicExpressionEvaluators.get(dialect);
        // Check if we know this dialect
        if (evaluator == null) {
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "The TopicExpression dialect {" + dialect +
                    "} was unknown.");
            return false;
        }
        return evaluator.evaluateTopicWithExpression(expression, topic);
    }

    /**
     * Tries to evaluate the given {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} with a single Topic
     * represented as a {@link javax.xml.namespace.QName}.
     *
     * @param topicExpressionType The <code>TopicExpressionType</code> to evaluate
     * @param context             The {@link javax.xml.namespace.NamespaceContext} the expression stands in
     * @return the <code>QName</code> of the Topic.
     * @throws UnsupportedOperationException                                   If the delegated evaluator is unable to identify topics on expression only. Try
     *                                                                         {@link org.ntnunotif.wsnu.base.topics.TopicExpressionEvaluatorInterface#getIntersection(org.oasis_open.docs.wsn.b_2.TopicExpressionType, org.oasis_open.docs.wsn.t_1.TopicSetType, javax.xml.namespace.NamespaceContext)}
     *                                                                         instead.
     * @throws InvalidTopicExpressionFault                                     If the content of the <code>TopicExpressionType</code> did not match the
     *                                                                         dialect specified.
     * @throws org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault       If more than one topic was identified by expression.
     * @throws org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault If the dialect was unknown by this validator
     */
    public static List<QName> evaluateTopicExpressionToQName(TopicExpressionType topicExpressionType, NamespaceContext context)
            throws UnsupportedOperationException, InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault,
            TopicExpressionDialectUnknownFault {
        // Delegating work
        String dialect = topicExpressionType.getDialect();
        TopicExpressionEvaluatorInterface evaluator = topicExpressionEvaluators.get(dialect);
        // Check if we know this dialect
        if (evaluator == null) {
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "The TopicExpression dialect {" + dialect +
                    "} was unknown.");
            return null;
        }
        return evaluator.evaluateTopicExpressionToQName(topicExpressionType, context);
    }

    /**
     * Adds a {@link org.ntnunotif.wsnu.base.topics.TopicExpressionEvaluatorInterface} the <code>TopicValidator</code>.
     * This is an easy way of adding validation for <code>TopicExpressionDialect</code>s not supported by default.
     *
     * @param evaluator the evaluator to add
     */
    public static void addTopicExpressionEvaluator(TopicExpressionEvaluatorInterface evaluator) {
        synchronized (TopicValidator.class) {
            topicExpressionEvaluators.put(evaluator.getDialectURIAsString(), evaluator);
        }
    }

    /**
     * Checks if the given expression is a legal expression with the validator.
     *
     * @param topicExpressionType the {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} to check.
     * @param namespaceContext    the {@link javax.xml.namespace.NamespaceContext} of this expression
     * @throws TopicExpressionDialectUnknownFault If the dialect of the expression was not recognized.
     * @throws InvalidTopicExpressionFault        if the content of the expression was malformed.
     */
    public static boolean isLegalExpression(TopicExpressionType topicExpressionType, NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // Delegating work
        String dialect = topicExpressionType.getDialect();
        TopicExpressionEvaluatorInterface evaluator = topicExpressionEvaluators.get(dialect);
        // Check if we know this dialect
        if (evaluator == null) {
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "The TopicExpression dialect {" + dialect +
                    "} was unknown.");
            return false;
        }
        return evaluator.isLegalExpression(topicExpressionType, namespaceContext);
    }

}
