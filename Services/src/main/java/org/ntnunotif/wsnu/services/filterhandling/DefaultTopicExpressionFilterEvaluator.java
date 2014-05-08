package org.ntnunotif.wsnu.services.filterhandling;

import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.ntnunotif.wsnu.base.topics.TopicValidator;
import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicSetType;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * The default evaluator used for topic expression filters used in WS-Nu.
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
    public boolean isWellFormed(Object filter, NamespaceContext namespaceContext) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        // Delegate work to TopicValidator
        return TopicValidator.isLegalExpression((TopicExpressionType)filter, namespaceContext);
    }

    @Override
    public Notify evaluate(Notify notify, NuNamespaceContextResolver notifyContextResolver, Object filter, NamespaceContext filterContext) {
        // Fast check if we can return directly
        if (notify == null)
            return null;

        // Check if filter is still correct
        if (filter.getClass() != filterClass()) {
            Log.e("FilterFail", "FilterEvaluator was used with illegal filter type");
            throw new IllegalArgumentException("FilterEvaluator was used with illegal filter type!");
        }

        TopicExpressionType _filter = (TopicExpressionType) filter;

        List<NotificationMessageHolderType> holderTypeList = notify.getNotificationMessage();

        for (int i = 0; i < holderTypeList.size(); i++) {
            NotificationMessageHolderType message = holderTypeList.get(i);

            TopicExpressionType topicExpression = message.getTopic();

            // If message does not contain a topic, it must be removed.
            if (topicExpression == null) {
                holderTypeList.remove(i--);
                continue;
            }

            try {
                List<QName> topicAsQNameList = TopicValidator.evaluateTopicExpressionToQName(
                        topicExpression, notifyContextResolver.resolveNamespaceContext(topicExpression)
                );
                Log.d("DefaultTopicExpressionFilterEvaluator", "Notify TopicExpression was topic: " + topicAsQNameList.toString());
                List<List<QName>> topic = new ArrayList<>();
                topic.add(topicAsQNameList);

                TopicSetType topicSetType = TopicUtils.qNameListListToTopicSet(topic);

                if (TopicValidator.getIntersection(_filter, topicSetType, filterContext) == null) {
                    holderTypeList.remove(i--);
                }

            } catch (InvalidTopicExpressionFault invalidTopicExpressionFault) {
                Log.e("DefaultTopicExpressionFilterEvaluator", "Ill formed TopicExpression either in Notify or Message");
                throw new IllegalArgumentException("The TopicExpression in the Notify was ill formed", invalidTopicExpressionFault);
            } catch (MultipleTopicsSpecifiedFault multipleTopicsSpecifiedFault) {
                Log.e("DefaultTopicExpressionFilterEvaluator", "Multiple Topics identified where only one was allowed " + message.getTopic().getContent());
                throw new IllegalArgumentException("The TopicExpression in the Notify described multiple Topics", multipleTopicsSpecifiedFault);
            } catch (TopicExpressionDialectUnknownFault topicExpressionDialectUnknownFault) {
                Log.e("DefaultTopicExpressionFilterEvaluator", "Did not recognize TopicDialect in either filter or message");
                throw new IllegalArgumentException("The dialect in the TopicExpression was unknown", topicExpressionDialectUnknownFault);
            }
        }

        // If we have emptied the notify, we can return null.
        return notify.getNotificationMessage().size() == 0 ? null : notify;
    }
}
