package org.ntnunotif.wsnu.services.implementations.notificationbroker;


import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEventSupport;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.AbstractNotificationProducer;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.brw_2.NotificationBroker;

import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_HAS_MESSAGE;
import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_OK;

/**
 * Created by tormod on 3/11/14.
 */
@javax.jws.WebService(targetNamespace = "http://docs.oasis-open.org/wsn/brw-2", name = "NotificationBroker")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public abstract class AbstractNotificationBroker extends AbstractNotificationProducer implements NotificationBroker {

    protected NotificationEventSupport _eventSupport = new NotificationEventSupport(this);

    protected AbstractNotificationBroker() {
        super();
    }

    protected AbstractNotificationBroker(Hub _hub) {
        super(_hub);
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
    public void sendSubscriptionRequest(String address){
        ObjectFactory factory = new ObjectFactory();
        Subscribe subscribe = factory.createSubscribe();

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        System.out.println(getEndpointReference());
        builder.address(getEndpointReference());

        W3CEndpointReference reference = builder.build();
        subscribe.setConsumerReference(reference);

        //subscribe.setInitialTerminationTime(factory.createSubscribeInitialTerminationTime("P1Y"));

        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, subscribe);
        message.getRequestInformation().setEndpointReference(address);
        _hub.acceptLocalMessage(message);
        System.out.println(message);
    }


}
