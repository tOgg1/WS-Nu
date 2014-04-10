package org.ntnunotif.wsnu.services.implementations.notificationconsumer;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEventSupport;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_HAS_MESSAGE;
import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_OK;

/**
 * The SimpleConsumer Web Service as defined per the Oasis WS-N Base specification
 * Created by tormod on 3/11/14.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "SimpleConsumer")
public class NotificationConsumer extends org.ntnunotif.wsnu.services.general.WebService implements org.oasis_open.docs.wsn.bw_2.NotificationConsumer{

    /**
     * Helper that deals with Notification events
     */
    private NotificationEventSupport _eventSupport = new NotificationEventSupport(this);


    public NotificationConsumer() {
        super();
    }

    /**
     * Constructor that takes in hub as an argument
     */
    public NotificationConsumer(Hub hub) {
        super(hub);
        _hub = hub;
    }


    @Override
    @WebMethod(operationName = "Notify")
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                           Notify notify) {
        _eventSupport.fireNotificationEvent(notify, _connection.getReqeustInformation());
    }

    @WebMethod(exclude = true)
    public void addConsumerListener(ConsumerListener listener){
        _eventSupport.addNotificationListener(listener);
    }

    @WebMethod(exclude = true)
    public void removeConsumerListener(ConsumerListener listener){
        _eventSupport.removeNotificationListener(listener);
    }

    @WebMethod(exclude = true)
    public InternalMessage sendSubscriptionRequest(Subscribe subscriptionRequest, String address){
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, subscriptionRequest);
        message.getRequestInformation().setEndpointReference(address);
        InternalMessage returnMessage = _hub.acceptLocalMessage(message);
        return returnMessage;
    }

    //@Override
    @WebMethod(exclude = true)
    public SoapForwardingHub quickBuild(String endpointReference) {
        try{
            SoapForwardingHub hub = new SoapForwardingHub();
            _hub = hub;

            /* Most reasonable and simple connector for a consumer */
            UnpackingConnector connector = new UnpackingConnector(this);
            hub.registerService(connector);
            _connection = connector;

            this.setEndpointReference(endpointReference);

            return hub;
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Could not quickBuild consumer: " + e.getMessage());
        }
    }
}
