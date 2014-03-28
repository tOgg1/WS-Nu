package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import junit.framework.TestCase;
import org.ntnunotif.wsnu.base.internal.ForwardingHub;

/**
 * Created by tormod on 25.03.14.
 */
public class SimpleNotificationProducerTest extends TestCase {

    public void testQuickBuild() throws Exception {
        SimpleNotificationProducer prod = new SimpleNotificationProducer();
        ForwardingHub hub = prod.quickBuild();
        hub.stop();
    }

    public void testQuickBuild1() throws Exception {

    }
}
