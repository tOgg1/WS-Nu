package org.ntnunotif.wsnu.examples.StockBroker;

import org.ntnunotif.wsnu.base.topics.TopicUtils;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 05.05.2014.
 */
public class StockTopics {

    public static final String STOCK_TOPIC_NAMESPACE = "http://www.sitronte.com/wsn/topics/examples/stocks";

    public static final String AFRICAN_STOCK_LOCAL_NAME = "Africa";
    public static final String AMERICAN_STOCK_LOCAL_NAME = "Africa";
    public static final String ASIAN_STOCK_LOCAL_NAME = "Africa";
    public static final String EUROPEAN_STOCK_LOCAL_NAME = "Africa";

    private static final String DEFAULT_NAMESPACE = "stockns";

    public static final TopicSetType COMPLETE_STOCK_TOPIC_SET;

    public static final TopicExpressionType AFRICAN_STOCK_TOPIC_EXPRESSION;
    public static final TopicExpressionType AMERICAN_STOCK_TOPIC_EXPRESSION;
    public static final TopicExpressionType ASIAN_STOCK_TOPIC_EXPRESSION;
    public static final TopicExpressionType EUROPEAN_STOCK_TOPIC_EXPRESSION;

    public static final List<QName> AFRICAN_STOCK_TOPIC_QNAME_LIST = new ArrayList<>();
    public static final List<QName> AMERICAN_STOCK_TOPIC_QNAME_LIST = new ArrayList<>();
    public static final List<QName> ASIAN_STOCK_TOPIC_QNAME_LIST = new ArrayList<>();
    public static final List<QName> EUROPEAN_STOCK_TOPIC_QNAME_LIST = new ArrayList<>();

    static {
        AFRICAN_STOCK_TOPIC_QNAME_LIST.add(new QName(STOCK_TOPIC_NAMESPACE, AFRICAN_STOCK_LOCAL_NAME, DEFAULT_NAMESPACE));
        AFRICAN_STOCK_TOPIC_EXPRESSION = TopicUtils.translateQNameListTopicToTopicExpression(AFRICAN_STOCK_TOPIC_QNAME_LIST);

        AMERICAN_STOCK_TOPIC_QNAME_LIST.add(new QName(STOCK_TOPIC_NAMESPACE, AMERICAN_STOCK_LOCAL_NAME, DEFAULT_NAMESPACE));
        AMERICAN_STOCK_TOPIC_EXPRESSION = TopicUtils.translateQNameListTopicToTopicExpression(AMERICAN_STOCK_TOPIC_QNAME_LIST);

        ASIAN_STOCK_TOPIC_QNAME_LIST.add(new QName(STOCK_TOPIC_NAMESPACE, ASIAN_STOCK_LOCAL_NAME, DEFAULT_NAMESPACE));
        ASIAN_STOCK_TOPIC_EXPRESSION = TopicUtils.translateQNameListTopicToTopicExpression(ASIAN_STOCK_TOPIC_QNAME_LIST);

        EUROPEAN_STOCK_TOPIC_QNAME_LIST.add(new QName(STOCK_TOPIC_NAMESPACE, EUROPEAN_STOCK_LOCAL_NAME, DEFAULT_NAMESPACE));
        EUROPEAN_STOCK_TOPIC_EXPRESSION = TopicUtils.translateQNameListTopicToTopicExpression(EUROPEAN_STOCK_TOPIC_QNAME_LIST);

        List<List<QName>> completeList = new ArrayList<>();
        completeList.add(AFRICAN_STOCK_TOPIC_QNAME_LIST);
        completeList.add(AMERICAN_STOCK_TOPIC_QNAME_LIST);
        completeList.add(ASIAN_STOCK_TOPIC_QNAME_LIST);
        completeList.add(EUROPEAN_STOCK_TOPIC_QNAME_LIST);

        COMPLETE_STOCK_TOPIC_SET = TopicUtils.qNameListListToTopicSet(completeList);
    }
}
