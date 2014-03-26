package org.ntnunotif.wsnu.services.implementations.notificationbroker;


import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.general.NotificationBroker;
import org.ntnunotif.wsnu.services.general.WebService;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.AbstractNotificationProducer;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationFailedFault;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationRejectedFault;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
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

    protected ArrayList<ConsumerListener> _listeners;

    protected AbstractNotificationBroker() {
        super();
        _listeners = new ArrayList<>();
    }

    protected AbstractNotificationBroker(Hub _hub) {
        super(_hub);
        _listeners = new ArrayList<>();
    }

    public void addConsumerListener(ConsumerListener listener){
        _listeners.add(listener);
    }

    public void removeConsumerListener(ConsumerListener listener){
        _listeners.remove(listener);
    }

}
