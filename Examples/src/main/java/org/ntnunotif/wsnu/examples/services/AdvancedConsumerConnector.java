package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.ServiceConnection;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.services.general.WebService;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.xmlsoap.schemas.soap.envelope.Envelope;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * This is a more advanced(yet simple) example, showing how you can implement a Web Service and a Connector in one go.
 * Effectively eliminating the need of having these two separate joints in the workflow.
 *
 * Created by tormod on 09.05.14.
 */
@javax.jws.WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "SimpleConsumer")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class AdvancedConsumerConnector extends WebService implements ServiceConnection, NotificationConsumer {

    private RequestInformation requestInformation;

    // A few constructors
    public AdvancedConsumerConnector() {
    }

    public AdvancedConsumerConnector(Hub hub) {
        super(hub);
    }

    /**
     * The implementation of NotificationConsumer's notify.
     */
    @Override
    @WebMethod(operationName = "Notify")
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Notify notify) {
        System.out.println("I got a notify!");
    }

    /**
     * Accept a net-message
     */
    @Override
    @WebMethod(exclude = true)
    public InternalMessage acceptMessage(InternalMessage message) {
        // On receiving a message from the hub, we assume it is parsed, and try to cast it to a Notify somehow.
        // If this does not work, we exit with a fault

        try{
            // Lets check if it is a standard envelope
            if(message.getMessage() instanceof Envelope){
                // Try and cast it to a notify
                Envelope envelope = (Envelope)message.getMessage();
                Notify receivedNotify = (Notify) envelope.getBody().getAny();

                // We call our own Web Method, notify.
                notify(receivedNotify);
            // It can also be this type of envelope
            } else if (message.getMessage() instanceof org.w3._2001._12.soap_envelope.Envelope){
                // Do essentially the same here

                org.w3._2001._12.soap_envelope.Envelope envelope = (org.w3._2001._12.soap_envelope.Envelope)message.getMessage();
                Notify receivedNotify = (Notify) envelope.getBody().getAny();
                notify(receivedNotify);
            } else if(message.getMessage() instanceof Notify) {
                // And the same here
                Notify receivedNotify = (Notify)message.getMessage();
                notify(receivedNotify);
            }
        }catch(Exception e){
            // Something went wrong.
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INTERNAL_ERROR, null);
        }
        // Everything went alright, return.
        return new InternalMessage(STATUS_OK, null);
    }

    /**
     * Accepts a pure request, do nothing.
     */
    @Override
    @WebMethod(exclude = true)
    public InternalMessage acceptRequest(InternalMessage message) {
        return new InternalMessage(STATUS_FAULT|STATUS_FAULT_NOT_SUPPORTED, null);
    }

    /**
     * Get the service type. In this scenario a NotificationConsumer
     */
    @Override
    @WebMethod(exclude = true)
    public Class getServiceType() {
        return NotificationConsumer.class;
    }

    /**
     * Get the endpointReference of the service.
     */
    @Override
    @WebMethod(exclude = true)
    public String getServiceEndpoint() {
        return getEndpointReference();
    }


    /**
     * Get the last requestInformation.
     */
    @Override
    @WebMethod(exclude = true)
    public RequestInformation getRequestInformation() {
        return requestInformation;
    }

    /**
     * This method does not apply here, as this class it the possible sender of such a message.
     */
    @Override
    @WebMethod(exclude = true)
    public void endpointUpdated(String newEndpointReference) {
        return;
    }
}
