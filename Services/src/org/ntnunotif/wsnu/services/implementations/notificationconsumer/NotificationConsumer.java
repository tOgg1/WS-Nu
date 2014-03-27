package org.ntnunotif.wsnu.services.implementations.notificationconsumer;

import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.internal.WebServiceConnector;
import org.ntnunotif.wsnu.base.util.*;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.activation.UnsupportedDataTypeException;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_HAS_MESSAGE;
import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_OK;

/**
 * The SimpleConsumer Web Service as defined per the Oasis WS-N Base specification
 * Created by tormod on 3/11/14.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "SimpleConsumer")
public class NotificationConsumer extends org.ntnunotif.wsnu.services.general.WebService implements org.oasis_open.docs.wsn.bw_2.NotificationConsumer{

    /**
     * All listeners to this SimpleConsumer.
     */
    private ArrayList<ConsumerListener> _listeners;

    /**
     * The EndpointReference of this NotificatonConsumer. This does not have to,
     * but can, be the same as the url/ip of the application server of the connected hub.
     */
    @EndpointReference(type = "uri")
    public String _endpointReference;

    public NotificationConsumer() {
        super();
        _listeners = new ArrayList<ConsumerListener>();
    }

    /**
     * Constructor that takes in hub as an argument
     */
    public NotificationConsumer(Hub hub) {
        super(hub);
        _hub = hub;
        _listeners = new ArrayList<>();
    }

    @Override
    public Object acceptSoapMessage(@WebParam Envelope envelope, @Information RequestInformation requestInformation) {
        return null;
    }

    @Override
    public InternalMessage acceptRequest(@Information RequestInformation requestInformation) {
        return null;
    }


    @Override
    @WebMethod(operationName = "Notify")
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Notify notify) {
        NotificationEvent event  = new NotificationEvent(notify);

        for(ConsumerListener listener : _listeners){
            listener.notify(event);
        }
    }

    public void addConsumerListener(ConsumerListener listener){
        _listeners.add(listener);
    }

    public void removeConsumerListener(ConsumerListener listener){
        _listeners.remove(listener);
    }

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
    }

    @Override
    public Hub quickBuild() {
        try{
            ForwardingHub hub = new ForwardingHub();
            /* Most reasonable and simple connector for a consumer */
            UnpackingConnector connector = new UnpackingConnector(this);
            hub.registerService(connector);
            this.registerConnection(connector);
            this._hub = hub;
            return hub;
        }catch(Exception e){
            throw new RuntimeException("Could not quickBuild consumer: " + e.getMessage());
        }
    }
}
