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
     * Evaluates the {@link org.oasis_open.docs.wsn.b_2.Notify} with the filter given. Returns true if the filter should
     * let the <code>Notify</code> pass through.
     *
     * @param notify the <code>Notify</code> to evaluate
     * @param filter the filter to evaluate with
     * @return <code>true</code> if the filter should allow the <code>Notify</code> through. <code>false</code>
     * otherwise
     */
    public boolean evaluate(Notify notify, Object filter);
}
