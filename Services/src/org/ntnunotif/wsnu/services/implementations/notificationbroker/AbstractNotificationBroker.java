package org.ntnunotif.wsnu.services.implementations.notificationbroker;


import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEventSupport;
import org.ntnunotif.wsnu.services.general.NotificationBroker;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.AbstractNotificationProducer;

import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;

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

    public void addConsumerListener(ConsumerListener listener){
        _eventSupport.addNotificationListener(listener);
    }

    public void removeConsumerListener(ConsumerListener listener){
        _eventSupport.removeNotificationListener(listener);
    }

}
