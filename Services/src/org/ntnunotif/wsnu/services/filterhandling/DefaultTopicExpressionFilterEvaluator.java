package org.ntnunotif.wsnu.services.filterhandling;

import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import javax.xml.namespace.QName;

/**
 * Created by Inge on 01.04.2014.
 */
public class DefaultTopicExpressionFilterEvaluator implements FilterEvaluator {
    private static final QName fName = new QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpression", "wsnt");

    @Override
    public Class filterClass() {
        return TopicExpressionType.class;
    }

    @Override
    public QName filterName() {
        return fName;
    }

    @Override
    public boolean evaluate(Notify notify, Object filter) {
        if (filter.getClass() != filterClass()) {
            Log.e("FilterFail", "FilterEvaluator was used with illegal filter type");
            throw new IllegalArgumentException("FilterEvaluator was used with illegal filter type!");
        }
        return false;
    }
}
