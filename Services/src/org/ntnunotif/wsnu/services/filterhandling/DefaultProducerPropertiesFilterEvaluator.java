package org.ntnunotif.wsnu.services.filterhandling;

import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * Created by Inge on 01.04.2014.
 */
public class DefaultProducerPropertiesFilterEvaluator implements FilterEvaluator {

    private static final QName fName = new QName("http://docs.oasis-open.org/wsn/b-2", "ProducerProperties", "wsnt");

    @Override
    public Class filterClass() {
        return QueryExpressionType.class;
    }

    @Override
    public QName filterName() {
        return fName;
    }

    @Override
    public boolean isWellFormed(Object filter, NamespaceContext namespaceContext) throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // TODO
        return false;
    }

    @Override
    public Notify evaluate(Notify notify, NamespaceContext notifyContext, Object filter, NamespaceContext filterContext) {
        // Fast check if we can return directly
        if (notify == null)
            return null;

        // Check if filter is still correct
        if (filter.getClass() != filterClass()) {
            Log.e("FilterFail", "FilterEvaluator was used with illegal filter type");
            throw new IllegalArgumentException("FilterEvaluator was used with illegal filter type!");
        }
        return null;
    }
}
