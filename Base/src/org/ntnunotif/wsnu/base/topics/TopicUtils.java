package org.ntnunotif.wsnu.base.topics;

import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 13.03.14.
 */
public class TopicUtils {
    /**
     * Should never be instantiated.
     */
    private TopicUtils() {}

    public static List<TopicType> topicSetToTopicTypeList(TopicSetType set, boolean includeChildren) {
        // TODO
        return new ArrayList<TopicType>();
    }

    public static TopicSetType topicTypeListToTopicSet(List<TopicType> list) {
        // TODO
        return null;
    }
}
