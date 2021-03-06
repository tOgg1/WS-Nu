//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

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
import java.util.List;

/**
 * A <code>TopicExpressionEvaluatorInterface</code> is an interface that should be implemented by objects that wishes
 * to compare a {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} with a given dialect to
 * {@link org.oasis_open.docs.wsn.t_1.TopicType}s.
 *
 * @author Inge Edward Halsaunet
 *         Created by Inge on 07.03.14.
 */
public interface TopicExpressionEvaluatorInterface {
    /**
     * Gets the dialect this <code>TopicExpressionEvaluatorInterface</code> can handle.
     *
     * @return The URI of the dialect as a String.
     */
    public String getDialectURIAsString();

    /**
     * Evaluates if the {@link org.oasis_open.docs.wsn.t_1.TopicType} is covered by the
     * {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}.
     *
     * @param topicExpressionType the expression to evaluate with
     * @param topicType           the Topic to evaluate
     * @return <code>true</code> if topic is covered. <code>false</code> otherwise.
     * @throws org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault If the dialect of the
     *                                                                         {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was unknown
     * @throws org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault        If the dialect of the
     *                                                                         {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was inconsistent with actual expression.
     */
    public boolean evaluateTopicWithExpression(TopicExpressionType topicExpressionType, TopicType topicType)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault;

    /**
     * Evaluates all {@link org.oasis_open.docs.wsn.t_1.TopicType}s contained in the
     * {@link org.oasis_open.docs.wsn.t_1.TopicSetType} with the {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}.
     *
     * @param topicExpressionType the expression to evaluate with.
     * @param topicSetType        the TopicSet to evaluate.
     * @param namespaceContext    the {@link javax.xml.namespace.NamespaceContext} of the expression
     * @return the set of all Topics in the queried set covered by the expression, <code>null</code> if none are covered
     * @throws org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault If the dialect of the
     *                                                                         {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was unknown
     * @throws org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault        If the dialect of the
     *                                                                         {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was inconsistent with actual expression.
     */
    public TopicSetType getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType, NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault;

    /**
     * * Evaluate the Topic a TopicExpression is describing is permitted in TopicNamespace given. Described in
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
    public boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault;

    /**
     * Tries to evaluate the given {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} with a single Topic
     * represented as a {@link javax.xml.namespace.QName}.
     *
     * @param topicExpressionType The <code>TopicExpressionType</code> to evaluate
     * @return the <code>QName</code> of the Topic.
     * @throws UnsupportedOperationException                                   If this evaluator is unable to identify topics on expression only. Try
     *                                                                         {@link org.ntnunotif.wsnu.base.topics.TopicExpressionEvaluatorInterface#getIntersection(org.oasis_open.docs.wsn.b_2.TopicExpressionType, org.oasis_open.docs.wsn.t_1.TopicSetType, javax.xml.namespace.NamespaceContext)}
     *                                                                         instead.
     * @throws org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault If the dialect of the
     *                                                                         {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType}
     *                                                                         was unknown
     * @throws InvalidTopicExpressionFault                                     If the content of the <code>TopicExpressionType</code> did not match the
     *                                                                         dialect specified.
     * @throws MultipleTopicsSpecifiedFault                                    If more than one topic was identified by expression.
     */
    public List<QName> evaluateTopicExpressionToQName(TopicExpressionType topicExpressionType, NamespaceContext context)
            throws UnsupportedOperationException, InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault,
            TopicExpressionDialectUnknownFault;

    /**
     * Checks if a given {@link org.oasis_open.docs.wsn.b_2.TopicExpressionType} is well formed and allowed.
     *
     * @param topicExpressionType the expression to check
     * @param namespaceContext    the {@link javax.xml.namespace.NamespaceContext} of the expression
     * @return <code>true</code> if legal, throws an {@link java.lang.Exception} otherwise
     * @throws TopicExpressionDialectUnknownFault if this <code>TopicExpressionEvaluatorInterface</code> did not recognize the dialect
     * @throws InvalidTopicExpressionFault        if the <code>TopicExpressionType</code> is malformed
     */
    public boolean isLegalExpression(TopicExpressionType topicExpressionType, NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault;
}
