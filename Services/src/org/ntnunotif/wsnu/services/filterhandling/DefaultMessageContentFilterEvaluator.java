package org.ntnunotif.wsnu.services.filterhandling;

import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.*;

/**
 * Created by Inge on 01.04.2014.
 */
public class DefaultMessageContentFilterEvaluator implements FilterEvaluator {
    private static final QName fName = new QName("http://docs.oasis-open.org/wsn/b-2", "MessageContent", "wsnt");

    private static final String dialectSupported = "http://www.w3.org/TR/1999/REC-xpath-19991116";

    @Override
    public Class filterClass() {
        return QueryExpressionType.class;
    }

    @Override
    public QName filterName() {
        return fName;
    }

    @Override
    public boolean isWellFormed(Object filter, NamespaceContext namespaceContext) throws
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault, InvalidMessageContentExpressionFault {

        // Check for correct use of evaluator
        if (filter instanceof QueryExpressionType) {
            QueryExpressionType queryExpressionFilter = (QueryExpressionType) filter;

            // Check dialect of expression, warnif not supported
            if (!dialectSupported.equals(queryExpressionFilter.getDialect())) {

                Log.w("DefaultMessageContentFilterEvaluator", "Asked to evaluate filter with unknown dialect: " +
                        queryExpressionFilter.getDialect());
                ServiceUtilities.throwInvalidMessageContentExpressionFault("en",
                        "Dialect used in filter was unknown. Accepts only " + dialectSupported + ", but found " +
                                queryExpressionFilter.getDialect()
                );
                return false;

            } else {
                // Dialect ok, extract actual expression
                try {
                    String expression = ServiceUtilities.extractQueryExpressionString(queryExpressionFilter);
                    // Build XPath environment
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    // Set up correct context
                    xPath.setNamespaceContext(namespaceContext);
                    // Try to compile the expression

                    xPath.compile(expression);
                    Log.d("DefaultMessageContentFilterEvaluator", "checked and accepted legal XPath expression: " + expression);
                    return true;

                } catch (IllegalArgumentException e) {
                    Log.w("DefaultMessageContentFilterEvaluator", "Malformed MessageContentFilter, can not " +
                            "understand complex or empty filters.");
                    return false;
                } catch (XPathExpressionException e) {
                    Log.w("DefaultMessageContentFilterEvaluator", "Was asked to check a malformed XPath expression");
                    ServiceUtilities.throwInvalidMessageContentExpressionFault("en", "MessageContextFilter did not " +
                            "follow correct XPath context");
                    return false;
                }

            }
        } else {
            Log.e("DefaultMessageContentFilterEvaluator", "Evaluator was used to evaluate wrong expression class");
            ServiceUtilities.throwInvalidMessageContentExpressionFault("en", "Filter claimed to be " + fName +
                    ", but was not parsed as such.");
            return false;
        }
    }

    @Override
    public Notify evaluate(Notify notify, NamespaceContext notifyContext, Object filter, NamespaceContext filterContext) {
        // Fast check if we can return directly
        if (notify == null)
            return null;

        // Check if filter is still correct, and no one is abusing this evaluator
        if (filter.getClass() != filterClass()) {
            Log.e("DefaultMessageContentFilterEvaluator", "FilterEvaluator was used with illegal filter type");
            throw new IllegalArgumentException("FilterEvaluator was used with illegal filter type!");
        }

        // For the rest, we assume content to be correct (except dialect); it should have been checked at subscription creation
        QueryExpressionType queryFilter = (QueryExpressionType) filter;
        if (!dialectSupported.equals(((QueryExpressionType) filter).getDialect())) {
            Log.e("DefaultMessageContentFilterEvaluator", "You should not evaluate filters that are malformed(dialect)");
            throw new IllegalArgumentException("Cannot evaluate unknown dialect in DefaultMessageContentFilterEvaluator");
        }

        String filterContent = ServiceUtilities.extractQueryExpressionString(queryFilter);

        // Build XPath environment
        XPath xPath = XPathFactory.newInstance().newXPath();
        // Set up correct context
        xPath.setNamespaceContext(filterContext);

        // Try to compile the expression
        try {
            XPathExpression compiled = xPath.compile(filterContent);

            // Go through all messages in filter, and see if it evaluates to true
            for (int i = 0; i < notify.getNotificationMessage().size(); i++) {

                try {
                    boolean evaluated = (boolean) compiled.evaluate(notify.getNotificationMessage().get(i).
                            getMessage().getAny(), XPathConstants.BOOLEAN);

                    if (!evaluated) {
                        notify.getNotificationMessage().remove(i--);
                    }

                } catch (XPathExpressionException e) {
                    Log.w("DefaultMessageContentFilterEvaluator", "A XPath expression failed to evaluate correctly. " +
                            "Might be non boolean: " + filterContent);
                }
            }
        } catch (XPathExpressionException e) {
            Log.e("DefaultMessageContentFilterEvaluator", "You should not evaluate filters that are " +
                    "malformed(XPath error)");
            e.printStackTrace();
        }

        // If we have emptied the notify, we can return null.
        return notify.getNotificationMessage().size() == 0 ? null : notify;
    }
}
