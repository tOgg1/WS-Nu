package org.ntnunotif.wsnu.services.filterhandling;

import org.oasis_open.docs.wsn.b_2.Notify;

import javax.xml.namespace.QName;

/**
 * The <code>FilterEvaluator</code> represents an {@link java.lang.Object} that is capable of determining if a specific
 * filter should allow a message, that is a {@link Notify} <code>Object</code> through.
 * Created by Inge on 01.04.2014.
 */
public interface FilterEvaluator {
    /**
     * Gives the class this filter type can handle as a filter
     *
     * @return the class this <code>FilterEvaluator</code> can handle
     */
    public Class filterClass();

    /**
     * Gives the {@link javax.xml.namespace.QName} the filter is identified with
     *
     * @return <code>QName</code> of the tag of the filter
     */
    public QName filterName();

    /**
     * Evaluates the {@link org.oasis_open.docs.wsn.b_2.Notify} with the filter given. Returns a <code>Notify</code>
     * element containing all accepted Notifications for this evaluator. WARNING the <code>Notify</code> argument may be
     * altered.
     *
     * @param notify the <code>Notify</code> to evaluate. The argument may be <code>null</code>, and may be altered
     * @param filter the filter to evaluate with
     * @return a <code>Notify</code> element containing all accepted Notifications. <code>null</code> if none was found.
     */
    public Notify evaluate(Notify notify, Object filter);
}
