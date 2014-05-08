package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.services.filterhandling.FilterEvaluator;
import org.ntnunotif.wsnu.services.filterhandling.FilterSupport;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * An example on how to extend the functionality for the FilterSupport
 */
public class FilterSupportExtension implements FilterEvaluator{

    private static final String PHONY_FILTER_NAMESPACE = "http://www.example.com/ws-nu/phony_filter";
    private static final String PHONY_FILTER_TAG_NAME = "NuPhonyFilter";
    private static final String PHONY_FILTER_DIALECT = "http://www.example.com/ws-nu/phony_filter_dialect";
    private static final String PHONY_FILTER_ONLY_ACCEPTABLE_CONTENT = "PhonyFilter";

    public static void main(String[] args) {

        // The WS-Nu implementation supports filtering on TopicExpressions and MessageContent. This filtering is done
        // in accordance with the OASIS-WS Notification specification. However one might wish to extend this
        // functionality by adding filters that filter on other properties. This example shows how such extension is
        // possible.
        //
        // The first thing that is necessary is to implement the FilterEvaluator interface. This class does this, and
        // description of the different methods are included as comments in the methods. Also check the JavaDoc on the
        // interface.
        //
        // We then create a FilterSupport

        FilterSupport filterSupport = new FilterSupport();

        // Now we register an instance of our evaluator to this filter support.
        FilterSupportExtension filterSupportExtension = new FilterSupportExtension();
        filterSupport.setFilterEvaluator(filterSupportExtension);

        // The instance we now registered with the filter support, is now responsible for filtering of all filtering of
        // this filter. That particular filter support ONLY supports the filter we have implemented. As for extending
        // the default, built in filter support, you may first create a default filter support object.

        FilterSupport defaultFilterExtended = FilterSupport.createDefaultFilterSupport();

        // And register an instance of the evaluator as before
        defaultFilterExtended.setFilterEvaluator(filterSupportExtension);
        // This filter support should now be able to filter on one additional filter.
    }

    @Override
    public Class filterClass() {
        // This method should return the class that the filter is represented with after parsing. This class is similar
        // to the DefaultMessageContentFilterEvaluator, it returns is a filter based on the QueryExpressionFilter class.
        return QueryExpressionType.class;
    }

    @Override
    public QName filterName() {
        // This method should return the qualified name of the filter as it stands in the xml.
        // Here the filter may be defined as
        // <ns:NuPhonyFilter Dialect="http://www.example.com/ws-nu/phony_filter_dialect"
        //     xmlns:ns="http://www.example.com/ws-nu/phony_filter" >
        //         PhonyFilter
        // </ns:NuPhonyFilter>
        // Of course, this is a phony filter, but the idea stay valid.
        return new QName(PHONY_FILTER_NAMESPACE, PHONY_FILTER_TAG_NAME);
    }

    @Override
    public boolean isWellFormed(Object filter, NamespaceContext namespaceContext) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault, InvalidMessageContentExpressionFault {
        // This method should check if the filter is well formed. That is, is it legal in all ways possible?
        // Since the PhonyFilter does not specify any special exceptions, we should just see if the dialect is correct,
        // and that it contains the correct string
        if (!(filter instanceof QueryExpressionType)) {
            return false;
        }

        // Check for dialect correctness
        QueryExpressionType phonyFilter = (QueryExpressionType) filter;
        if (!PHONY_FILTER_DIALECT.equals(phonyFilter.getDialect())) {
            return false;
        }

        // Extract the filter string from the filter (may cause IllegalArgumentException)
        try {
            String filterContent = ServiceUtilities.extractQueryExpressionString(phonyFilter);

            // The check if the content of the filter was correct
            return PHONY_FILTER_ONLY_ACCEPTABLE_CONTENT.equals(filterContent);
        } catch (RuntimeException re) {
            // If there was an IllegalArgumentException, the filter was a nested structure -> it did not only contain
            // simple text.
            return false;
        }
    }

    @Override
    public Notify evaluate(Notify notify, NuNamespaceContextResolver notifyContextResolver, Object filter,
                           NamespaceContext filterContext) {

        // This method should filter out the messages from the Notify that are legal according to this filter.
        // If the notify is null, we may directly return null
        if (notify == null) {
            return null;
        }

        // This makes certain that the filter in question is actually the correct data type.
        if (filter.getClass() != filterClass()) {
            throw new IllegalArgumentException("FilterEvaluator was used with illegal filter type!");
        }

        // Cast the filter to the correct class
        QueryExpressionType phonyFilter = (QueryExpressionType) filter;

        // Get the messages contained in the filter, so we can filter out the messages that are correct
        List<NotificationMessageHolderType> holderTypeList = notify.getNotificationMessage();

        // Go through the list of messages, and filter out the ones that are not in accordance with the filter.
        for (int i = 0; i < holderTypeList.size(); i++) {

            // Get the current message holder
            NotificationMessageHolderType messageHolder = holderTypeList.get(i);

            // Get the actual message
            NotificationMessageHolderType.Message message = messageHolder.getMessage();

            // Check if the message is a String, if not, this should be removed (simple filter -> simple message)
            if (!(message.getAny() instanceof String)) {
                // Remove an post decrement counter.
                holderTypeList.remove(i--);
            }
        }

        // Now, if there are no more messages left in the notify, we can return null
        if (notify.getNotificationMessage().size() == 0) {
            return null;
        }

        // Else we just return the notify as it is
        return notify;
    }
}
