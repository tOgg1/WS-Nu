package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;

/**
 * Created by tormod on 25.03.14.
 */
public class SimpleNotificationProducerTest {

    @Test
    public void testQuickBuild() throws Exception {
        SimpleNotificationProducer prod = new SimpleNotificationProducer();
        SoapForwardingHub hub = prod.quickBuild("simpleNot");
        //hub.stop();
    }

    @Test
    public void testQuickBuild1() throws Exception {

    }
}
