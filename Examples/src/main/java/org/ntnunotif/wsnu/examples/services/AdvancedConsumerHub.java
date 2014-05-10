package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.ServiceConnection;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.general.WebService;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.xmlsoap.schemas.soap.envelope.Envelope;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.OutputStream;
import java.util.Collection;

/**
 * This is an advanced example, where a NotificationConsumer implements the Hub-interface, effectively eliminating
 * the need for a hub and a connector.
 *
 * Created by tormod on 09.05.14.
 */
@javax.jws.WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "SimpleConsumer")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class AdvancedConsumerHub extends WebService implements Hub, NotificationConsumer {

    // A constructor starting the ApplicationServer
    public AdvancedConsumerHub() {
        try {
            ApplicationServer server = ApplicationServer.getInstance();
            // Pass in this as the Hub argument
            server.start(this);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    /**
     * Implementation of NotificationConsumer's notify.
     */
    @Override
    @WebMethod(operationName = "Notify")
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Notify notify) {
        System.out.println("I got a notify!");
    }

    /**
     * Implementation of Hub's acceptNetMessage, i.e. the method called from the ApplicationServer on receiving a request.
     */
    @Override
    public InternalMessage acceptNetMessage(InternalMessage message, OutputStream streamToRequestor) {
        try {
            // This flavour of parse parses the contents of the InternalMessage and adds the result to itself.
            XMLParser.parse(message);

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

        } catch (JAXBException e) {
            return new InternalMessage(InternalMessage.STATUS_FAULT, null);
        }
        return new InternalMessage(InternalMessage.STATUS_FAULT, null);
    }

    /**
     * Accept a local message, we don't care about that here.
     */
    @Override
    public InternalMessage acceptLocalMessage(InternalMessage message) {
        return new InternalMessage(InternalMessage.STATUS_FAULT, null);
    }

    /**
     * The address of the Hub is the endpointReference of the WebService.
     */
    @Override
    public String getInetAdress() {
        return getEndpointReference();
    }

    // We are going to ignore all the methods below, as they don't have any meaning here.
    @Override
    public void registerService(ServiceConnection webServiceConnector) {
        return;
    }

    @Override
    public void removeService(ServiceConnection webServiceConnector) {
        return;
    }

    @Override
    public boolean isServiceRegistered(ServiceConnection webServiceConnector) {
        return false;
    }

    @Override
    public Collection<ServiceConnection> getServices() {
        return null;
    }


}
