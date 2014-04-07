package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import junit.framework.TestCase;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;

/**
 * Created by tormod on 25.03.14.
 */
public class SimpleNotificationProducerTest extends TestCase {

    public void testQuickBuild() throws Exception {
        SimpleNotificationProducer prod = new SimpleNotificationProducer();
        SoapForwardingHub hub = prod.quickBuild("simpleNot");
        hub.stop();
    }

    public void testQuickBuild1() throws Exception {

    }
}
