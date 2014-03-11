package org.ntnunotif.wsnu.services.NotificationConsumer;

import org.oasis_open.docs.wsn.b_2.Notify;

import javax.jws.WebParam;

/**
 * Created by tormod on 3/11/14.
 */
public class NotificationConsumer implements org.oasis_open.docs.wsn.bw_2.NotificationConsumer {
    @Override
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Notify notify) {

    }
}
