package org.ntnunotif.wsnu.services.general;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.filterhandling.FilterSupport;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Inge on 02.04.2014.
 */
public class FilterTest {
    private static final String subscribeWithFilterLocation = "Services/res/server_test_subscribe.xml";
    private static final String largeNotifyLocation = "Services/res/filter_test_large_notify.xml";
    private static final String allFilterLocation = "Services/res/filter_test_filter_collection.xml";

    private static InternalMessage subscribeInternalMessage;
    private static FilterType filterType;
    private static Notify notifySource;
    private static NamespaceContext notifyContext;
    private static FilterType allFilters;
    private static NamespaceContext filterContext;

    private static FilterSupport defaultFilterSupport;

    @BeforeClass
    public static void globalSetup() {

        // Create filter support
        defaultFilterSupport = FilterSupport.createDefaultFilterSupport();

        // read in data from files
        try {
            subscribeInternalMessage = XMLParser.parse(new FileInputStream(subscribeWithFilterLocation));
            JAXBElement<Envelope> element = (JAXBElement<Envelope>)subscribeInternalMessage.getMessage();
            Body b = element.getValue().getBody();
            filterType = ((Subscribe)b.getAny().get(0)).getFilter();
            InternalMessage internalMessage = XMLParser.parse(new FileInputStream(largeNotifyLocation));
            notifyContext = internalMessage.getRequestInformation().getNamespaceContext();
            notifySource = (Notify)internalMessage.getMessage();

            internalMessage= XMLParser.parse(new FileInputStream(allFilterLocation));
            filterContext = internalMessage.getRequestInformation().getNamespaceContext();
            allFilters = (FilterType)((JAXBElement)internalMessage.getMessage()).getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExamineFilterContent() {
        Assert.assertNotNull("Filter was null, cannot inspect", filterType);
        for (Object o : filterType.getAny()) {
            System.out.println("New filter:");
            if (o instanceof JAXBElement) {
                JAXBElement je = (JAXBElement) o;
                System.out.println("Declared type: " + je.getDeclaredType());
                System.out.println("Scope: " + je.getScope());
                System.out.println("Name: " + je.getName());
            }
        }
    }

    @Test
    public void testFilterSimpleTopics() {
        // Create necessary elements, do an evaluation and check if result is correct

        // Filters 1-3 are simple topics in XML
        JAXBElement filter1 = (JAXBElement)allFilters.getAny().get(0);
        JAXBElement filter2 = (JAXBElement)allFilters.getAny().get(1);
        JAXBElement filter3 = (JAXBElement)allFilters.getAny().get(2);

        // Create Maps that can build their Subscription info
        Map<QName, Object> filterMap1 = new HashMap<>();
        Map<QName, Object> filterMap2 = new HashMap<>();
        Map<QName, Object> filterMap3 = new HashMap<>();
        filterMap1.put(filter1.getName(), filter1.getValue());
        filterMap2.put(filter2.getName(), filter2.getValue());
        filterMap3.put(filter3.getName(), filter3.getValue());

        // Build SubscriptionInfo
        FilterSupport.SubscriptionInfo subscriptionInfo1 = new FilterSupport.SubscriptionInfo(filterMap1, filterContext);
        FilterSupport.SubscriptionInfo subscriptionInfo2 = new FilterSupport.SubscriptionInfo(filterMap2, filterContext);
        FilterSupport.SubscriptionInfo subscriptionInfo3 = new FilterSupport.SubscriptionInfo(filterMap3, filterContext);

        // Do evaluation
        Notify notify1 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo1, notifyContext);
        Notify notify2 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo2, notifyContext);
        Notify notify3 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo3, notifyContext);

        try {
            XMLParser.writeObjectToStream(notify1, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        // First two filtered results should not be null
        Assert.assertNotNull("First Simple filter evaluated notify to null", notify1);
        Assert.assertNotNull("Second Simple filter evaluated notify to null", notify2);

        // Filter 1 and 2 should evaluate to 1 element
        Assert.assertEquals("First simple filter failed notify elements evaluation", 1, notify1.getNotificationMessage().size());
        Assert.assertEquals("Second simple filter failed notify elements evaluation", 1, notify2.getNotificationMessage().size());
        // Filter 3 should not evaluate to any elements
        Assert.assertNull("Third simple filter failed notify elements evaluation", notify3);
    }

    @Test
    public void testFilterConcreteTopics() {
        // Create necessary elements, do an evaluation and check if result is correct

        // Filters 4-6 are concrete topics in XML
        JAXBElement filter1 = (JAXBElement)allFilters.getAny().get(3);
        JAXBElement filter2 = (JAXBElement)allFilters.getAny().get(4);
        JAXBElement filter3 = (JAXBElement)allFilters.getAny().get(5);

        // Create Maps that can build their Subscription info
        Map<QName, Object> filterMap1 = new HashMap<>();
        Map<QName, Object> filterMap2 = new HashMap<>();
        Map<QName, Object> filterMap3 = new HashMap<>();
        filterMap1.put(filter1.getName(), filter1.getValue());
        filterMap2.put(filter2.getName(), filter2.getValue());
        filterMap3.put(filter3.getName(), filter3.getValue());

        // Build SubscriptionInfo
        FilterSupport.SubscriptionInfo subscriptionInfo1 = new FilterSupport.SubscriptionInfo(filterMap1, filterContext);
        FilterSupport.SubscriptionInfo subscriptionInfo2 = new FilterSupport.SubscriptionInfo(filterMap2, filterContext);
        FilterSupport.SubscriptionInfo subscriptionInfo3 = new FilterSupport.SubscriptionInfo(filterMap3, filterContext);

        // Do evaluation
        Notify notify1 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo1, notifyContext);
        Notify notify2 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo2, notifyContext);
        Notify notify3 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo3, notifyContext);

        // First two filtered results should not be null
        Assert.assertNotNull("First Concrete filter evaluated notify to null", notify1);
        Assert.assertNotNull("Second Concrete filter evaluated notify to null", notify2);

        // Filter 1 and 2 should evaluate to 3 elements
        Assert.assertEquals("First concrete filter failed notify elements evaluation", 3, notify1.getNotificationMessage().size());
        Assert.assertEquals("Second concrete filter failed notify elements evaluation", 3, notify2.getNotificationMessage().size());
        // Filter 3 should not evaluate to any elements
        Assert.assertNull("Third concrete filter failed notify elements evaluation", notify3);
    }

    @Test
    public void testFilterFullTopics() {
        // Create necessary elements, do an evaluation and check if result is correct

        // Filters 7-9 are full topics in XML
        JAXBElement filter1 = (JAXBElement)allFilters.getAny().get(6);
        JAXBElement filter2 = (JAXBElement)allFilters.getAny().get(7);
        JAXBElement filter3 = (JAXBElement)allFilters.getAny().get(8);

        // Create Maps that can build their Subscription info
        Map<QName, Object> filterMap1 = new HashMap<>();
        Map<QName, Object> filterMap2 = new HashMap<>();
        Map<QName, Object> filterMap3 = new HashMap<>();
        filterMap1.put(filter1.getName(), filter1.getValue());
        filterMap2.put(filter2.getName(), filter2.getValue());
        filterMap3.put(filter3.getName(), filter3.getValue());

        // Build SubscriptionInfo
        FilterSupport.SubscriptionInfo subscriptionInfo1 = new FilterSupport.SubscriptionInfo(filterMap1, filterContext);
        FilterSupport.SubscriptionInfo subscriptionInfo2 = new FilterSupport.SubscriptionInfo(filterMap2, filterContext);
        FilterSupport.SubscriptionInfo subscriptionInfo3 = new FilterSupport.SubscriptionInfo(filterMap3, filterContext);

        // Do evaluation
        Notify notify1 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo1, notifyContext);
        Notify notify2 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo2, notifyContext);
        Notify notify3 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo3, notifyContext);

        // First two filtered results should not be null
        Assert.assertNotNull("First full filter evaluated notify to null", notify1);
        Assert.assertNotNull("Second full filter evaluated notify to null", notify2);

        // Filter 1 should evaluate to 8 elements
        Assert.assertEquals("First full filter failed notify elements evaluation", 8, notify1.getNotificationMessage().size());
        // Filter 2 should evaluate to 3 elements
        Assert.assertEquals("Second full filter failed notify elements evaluation", 3, notify2.getNotificationMessage().size());
        // Filter 3 should not evaluate to any elements
        Assert.assertNull("Third full filter failed notify elements evaluation", notify3);
    }

    @Test
    public void testFilterXPathTopics() {
        // Create necessary elements, do an evaluation and check if result is correct

        // Filters 10-12 are xpath topics in XML
        JAXBElement filter1 = (JAXBElement)allFilters.getAny().get(9);
        JAXBElement filter2 = (JAXBElement)allFilters.getAny().get(10);
        JAXBElement filter3 = (JAXBElement)allFilters.getAny().get(11);

        // Create Maps that can build their Subscription info
        Map<QName, Object> filterMap1 = new HashMap<>();
        Map<QName, Object> filterMap2 = new HashMap<>();
        Map<QName, Object> filterMap3 = new HashMap<>();
        filterMap1.put(filter1.getName(), filter1.getValue());
        filterMap2.put(filter2.getName(), filter2.getValue());
        filterMap3.put(filter3.getName(), filter3.getValue());

        // Build SubscriptionInfo
        FilterSupport.SubscriptionInfo subscriptionInfo1 = new FilterSupport.SubscriptionInfo(filterMap1, filterContext);
        FilterSupport.SubscriptionInfo subscriptionInfo2 = new FilterSupport.SubscriptionInfo(filterMap2, filterContext);
        FilterSupport.SubscriptionInfo subscriptionInfo3 = new FilterSupport.SubscriptionInfo(filterMap3, filterContext);

        // Do evaluation
        Notify notify1 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo1, notifyContext);
        Notify notify2 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo2, notifyContext);
        Notify notify3 = defaultFilterSupport.evaluateNotifyToSubscription(notifySource, subscriptionInfo3, notifyContext);

        // First two filtered results should not be null
        Assert.assertNotNull("First XPath filter evaluated notify to null", notify1);
        Assert.assertNotNull("Second XPath filter evaluated notify to null", notify2);
        Assert.assertNull("Third XPath filter should evaluate to null", notify3);

        // Filter 1 should evaluate to 8 elements
        Assert.assertEquals("First xpath filter failed notify elements evaluation", 8, notify1.getNotificationMessage().size());
        // Filter 2 should evaluate to 3 elements
        Assert.assertEquals("Second xpath filter failed notify elements evaluation", 3, notify2.getNotificationMessage().size());
    }

    @Test
    public void testFilterMessageContent() {
        // TODO
    }

    @Test
    public void testIllegalFilter() {
        // TODO
    }
}
