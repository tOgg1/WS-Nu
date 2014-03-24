package org.ntnunotif.wsnu.base.topics;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.oasis_open.docs.wsn.b_2.MultipleTopicsSpecifiedFaultType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.t_1.TopicNamespaceType;
import org.oasis_open.docs.wsn.t_1.TopicSetType;
import org.oasis_open.docs.wsn.t_1.TopicType;
import org.oasis_open.docs.wsrf.bf_2.BaseFaultType;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Inge on 21.03.2014.
 */
public class FullEvaluator implements TopicExpressionEvaluatorInterface {
    /**
     * The dialect this evaluator supports
     */
    public static final String dialectURI = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full";

    @Override
    public String getDialectURIAsString() {
        return dialectURI;
    }

    @Override
    public boolean evaluateTopicWithExpression(TopicExpressionType topicExpressionType, TopicType topicType)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        return false;
    }

    @Override
    public TopicSetType getIntersection(TopicExpressionType topicExpressionType, TopicSetType topicSetType,
                                        NamespaceContext namespaceContext)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {

        if (!dialectURI.equals(topicExpressionType.getDialect()))
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "Full evaluator can evaluate Full dialect!");

        // Fetch expression
        String expression = TopicUtils.extractExpression(topicExpressionType);
        if (!isFullDialect(expression)) {
            TopicUtils.throwInvalidTopicExpressionFault("en",
                    "Topic expression claimed to be full dialect, but was not");
        }

        // Use XPath evaluator to do the work
        return XPathEvaluator.getXpathIntersection(expression, topicSetType, namespaceContext);
    }

    @Override
    public boolean isExpressionPermittedInNamespace(TopicExpressionType expression, TopicNamespaceType namespace)
            throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        return false;
    }

    @Override
    public List<QName> evaluateTopicExpressionToQName(TopicExpressionType topicExpressionType, NamespaceContext context)
            throws UnsupportedOperationException, InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault,
            TopicExpressionDialectUnknownFault {
        if (!dialectURI.equals(topicExpressionType.getDialect()))
            TopicUtils.throwTopicExpressionDialectUnknownFault("en", "Full evaluator can evaluate Full dialect!");

        String expression = TopicUtils.extractExpression(topicExpressionType);

        if (!isFullDialect(expression)) {
            TopicUtils.throwInvalidTopicExpressionFault("en",
                    "The dialect in expression could not be identified as a Full Dialect");
        }

        return evaluateFullTopicExpressionToQNameList(expression, context);
    }

    private static void throwMultipleTopicsSpecifiedFault(String lang, String desc) throws MultipleTopicsSpecifiedFault {
        MultipleTopicsSpecifiedFaultType faultType = new MultipleTopicsSpecifiedFaultType();
        faultType.setTimestamp(new XMLGregorianCalendarImpl(new GregorianCalendar(TimeZone.getTimeZone("UTC"))));
        BaseFaultType.Description description = new BaseFaultType.Description();
        description.setLang(lang);
        description.setValue(desc);
        faultType.getDescription().add(description);
        throw new MultipleTopicsSpecifiedFault(desc, faultType);
    }

    public static List<QName> evaluateFullTopicExpressionToQNameList(String expression, NamespaceContext context)
            throws InvalidTopicExpressionFault, MultipleTopicsSpecifiedFault {
        if (expression == null || expression.length() == 0)
            TopicUtils.throwInvalidTopicExpressionFault("en", "No expression given to evaluate");

        List<QName> qNames = new ArrayList<>();

        // Parse expression to single QName list
        BuildState state = BuildState.Start;
        String nc1 = "", nc2 = "";
        for (int i = 0; expression!= null && i < expression.length(); i++) {
            char c = expression.charAt(i);
            switch (state) {
                case Start:
                    if (isNCStart(c)) {
                        nc1 = "" + c;
                        state = BuildState.NC1;
                    } else {
                        throwMultipleTopicsSpecifiedFault("en", "Either no root was specified, or a wildcard was used");
                    }
                    break;
                case NC1:
                    if (isNC(c)) {
                        nc1 += c;
                    } else if (c == '/') {
                        state = BuildState.Start;
                        qNames.add(new QName(nc1));
                    } else if (c == ':') {
                        state = BuildState.NC2Start;
                    } else {
                        throwMultipleTopicsSpecifiedFault("en", "Could not define specific topic at character " + c);
                    }
                    break;
                case NC2Start:
                    if (isNCStart(c)) {
                        nc2 = "" + c;
                        state = BuildState.NC2;
                    } else {
                        throwMultipleTopicsSpecifiedFault("en", "Could not define specific topic at character " + c);
                    }
                    break;
                case NC2:
                    if (isNC(c)) {
                        nc2 += c;
                    } else if (c == '/') {
                        String ns = context.getNamespaceURI(nc1);
                        if (ns == null) {
                            TopicUtils.throwInvalidTopicExpressionFault("en",
                                    "Prefix could not be resolved to a namespace");
                        }
                        qNames.add(new QName(ns, nc2));
                        state = BuildState.Start;
                    } else {
                        throwMultipleTopicsSpecifiedFault("en", "Could not define specific topic at character " + c);
                    }
                    break;
            }
            //
        }
        if (state == BuildState.Start || state == BuildState.NC2Start)
            TopicUtils.throwInvalidTopicExpressionFault("en", "Could not identify QNames from given expression");
        return qNames;
    }

    private static boolean isFullDialect(String expression) {
        // A simple check, do we need to proceed?
        if (expression == null || expression.length() == 0)
            return false;
        CheckState state = CheckState.RootStart;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            switch (state) {
                case RootStart:
                    if (c == '*')
                        state = CheckState.ChildReady;
                    else if (c == '/')
                        state = CheckState.RootPostPre1;
                    else if (isNCStart(c))
                        state = CheckState.RootNC1;
                    else
                        return false;
                    break;
                case RootPrefixed:
                    if (c == '/')
                        state = CheckState.RootPostPre1;
                    else if (c == '*')
                        state = CheckState.ChildReady;
                    else if (isNCStart(c))
                        state = CheckState.RootNC2;
                    else
                        return false;
                    break;
                case RootPostPre1:
                    if (c == '/')
                        state = CheckState.RootPostPre2;
                    else
                        return false;
                    break;
                case RootPostPre2:
                    if (c == '*')
                        state = CheckState.ChildReady;
                    else if (isNCStart(c))
                        state = CheckState.RootNC2;
                    else
                        return false;
                    break;
                case RootNC1:
                    if (c == ':')
                        state = CheckState.RootPrefixed;
                    else if (c == '|')
                        state = CheckState.RootStart;
                    else if (c == '/')
                        state = CheckState.ChildStart;
                    else if (!isNC(c))
                        return false;
                    break;
                case RootNC2:
                    if (c == '|')
                        state = CheckState.RootStart;
                    else if (c == '/')
                        state = CheckState.ChildStart;
                    else if (!isNC(c))
                        return false;
                    break;
                case ChildReady:
                    if (c == '|')
                        state = CheckState.RootStart;
                    else if (c == '/')
                        state = CheckState.ChildStart;
                    else
                        return false;
                    break;
                case ChildStart:
                    if (c == '/')
                        state = CheckState.ChildStart2;
                    else if (c == '*' || c == '.')
                        state = CheckState.ChildReady;
                    else if (isNCStart(c))
                        state = CheckState.ChildNC1;
                    else
                        return false;
                    break;
                case ChildStart2:
                    if (c == '*' || c == '.')
                        state = CheckState.ChildReady;
                    else if (isNCStart(c))
                        state = CheckState.ChildNC1;
                    else
                        return false;
                    break;
                case ChildNC1:
                    if (c == '|')
                        state = CheckState.RootStart;
                    else if (c == '/')
                        state = CheckState.ChildStart;
                    else if (c == ':')
                        state = CheckState.ChildPre;
                    else if (!isNC(c))
                        return false;
                    break;
                case ChildPre:
                    if (isNCStart(c))
                        state = CheckState.ChildNC2;
                    else
                        return false;
                    break;
                case ChildNC2:
                    if (c == '/')
                        state = CheckState.ChildStart;
                    else if (c == '|')
                        state = CheckState.RootStart;
                    else if (!isNC(c))
                        return false;
                    break;
            }
        }
        switch (state) {
            case RootNC1:
            case RootNC2:
            case ChildReady:
            case ChildNC1:
            case ChildNC2:
                return true;
            default:
                return false;
        }
    }

    private static boolean isNCStart(char c) {
        // Upper case
        if (c >= 'A' && c <= 'Z')
            return true;
        // lower case
        if (c >= 'a' && c <= 'z')
            return true;
        // underscore
        return c == '_';
    }

    private static boolean isNC(char c) {
        // hyphen and punctuation
        if (c == '-' || c == '.')
            return true;
        // Number
        return c >= '0' && c <= '9' || isNCStart(c);
    }

    private enum CheckState {
        RootStart, RootPrefixed, RootPostPre1, RootPostPre2, RootNC1, RootNC2, ChildReady, ChildStart, ChildStart2,
        ChildNC1, ChildPre, ChildNC2
    }

    private enum BuildState {
        Start, NC1, NC2Start, NC2
    }
}
