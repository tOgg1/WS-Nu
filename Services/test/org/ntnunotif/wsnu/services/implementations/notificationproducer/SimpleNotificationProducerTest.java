package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import junit.framework.TestCase;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

/**
 * Created by tormod on 25.03.14.
 */
public class SimpleNotificationProducerTest extends TestCase {
    public void testQuickBuild() throws Exception {
        SimpleNotificationProducer prod = new SimpleNotificationProducer();
        Hub hub = prod.quickBuild();

        Envelope env = new Envelope();
        Body body = new Body();
        body.getAny().add(new Subscribe());
        env.setBody(body);

    }

    public void testQuickBuild1() throws Exception {

    }
}
