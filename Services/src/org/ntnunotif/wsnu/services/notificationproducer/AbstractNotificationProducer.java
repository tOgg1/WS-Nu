package org.ntnunotif.wsnu.services.notificationproducer;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.general.WebService;
import org.ntnunotif.wsnu.services.interfaces.NotificationProducer;
import org.oasis_open.docs.wsn.b_2.*;

import java.io.InputStream;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Created by tormod on 3/11/14.
 */
public abstract class AbstractNotificationProducer implements NotificationProducer, WebService {

    private final Hub _hub;

    /**
     * Default and only constructor. This does not have to called if the hub is set
     * @param hub
     */
    public AbstractNotificationProducer(Hub hub) {
        _hub = hub;
    }

    /**
     * Sends a notification the the endpoint.
     * @param notify
     * @param endPoint Endpoint of the recipient. Can be formatted as an IPv4, IPv6 or URL adress.
     */
    public void sendNotification(Notify notify, String endPoint)
    {
        _hub.acceptLocalMessage(new InternalMessage(STATUS_OK|STATUS_HAS_RETURNING_MESSAGE, notify), endPoint);
    }

    /**
     * Sends a notification the the endpoint.
     * @param notify
     * @param endPoint Endpoint of the recipient. Can be formatted as an IPv4, IPv6 or URL adress.
     */
    public void sendNotification(String notify, String endPoint)
    {
        _hub.acceptLocalMessage(new InternalMessage(STATUS_OK|STATUS_HAS_RETURNING_MESSAGE, notify), endPoint);
    }

    /**
     * Sends a notification the the endpoint.
     * @param notify
     * @param endPoint Endpoint of the recipient. Can be formatted as an IPv4, IPv6 or URL adress.
     */
    public void sendNotification(InputStream notify, String endPoint)
    {
        _hub.acceptLocalMessage(new InternalMessage(STATUS_OK|STATUS_HAS_RETURNING_MESSAGE|STATUS_RETURNING_MESSAGE_IS_INPUTSTREAM, notify), endPoint);
    }
}
