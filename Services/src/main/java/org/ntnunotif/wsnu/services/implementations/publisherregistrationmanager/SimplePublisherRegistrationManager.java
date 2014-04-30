package org.ntnunotif.wsnu.services.implementations.publisherregistrationmanager;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.services.eventhandling.PublisherRegistrationEvent;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.br_2.DestroyRegistration;
import org.oasis_open.docs.wsn.br_2.DestroyRegistrationResponse;
import org.oasis_open.docs.wsn.brw_2.ResourceNotDestroyedFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tormod on 23.04.14.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/brw-2", name = "PublisherRegistrationManager")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class SimplePublisherRegistrationManager extends AbstractPublisherRegistrationManager {

    private HashMap<String, Long> _publishers;

    public SimplePublisherRegistrationManager() {
    }

    public SimplePublisherRegistrationManager(Hub hub) {
        super(hub);
    }

    @Override
    @WebMethod(exclude = true)
    public void addPublisher(String endpointReference, long subscriptionEnd) {
        _publishers.put(endpointReference, subscriptionEnd);
    }

    @Override
    @WebMethod(exclude = true)
    public void removePublisher(String endpointReference) {
        _publishers.remove(endpointReference);
    }

    @Override
    @WebResult(name = "DestroyRegistrationResponse", targetNamespace = "http://docs.oasis-open.org/wsn/br-2", partName = "DestroyRegistrationResponse")
    @WebMethod(operationName = "DestroyRegistration")
    public DestroyRegistrationResponse destroyRegistration
    (
        @WebParam(partName = "DestroyRegistrationRequest", name = "DestroyRegistration", targetNamespace = "http://docs.oasis-open.org/wsn/br-2")
        DestroyRegistration destroyRegistrationRequest
    ) throws ResourceNotDestroyedFault, ResourceUnknownFault {
        Log.d("SimplePublishersRegistrationManager", "Received DestroyRegistration request");
        RequestInformation requestInformation = _connection.getRequestInformation();

        for (Map.Entry<String, String[]> entry : requestInformation.getParameters().entrySet()) {
            if(!entry.getKey().equals("subscription")){
                continue;
            }

            if(entry.getValue().length > 1){
                String subRef = entry.getValue()[0];
                if(!_publishers.containsKey(subRef)){
                    _publishers.remove(subRef);
                    return new DestroyRegistrationResponse();
                }
                ServiceUtilities.throwResourceUnknownFault("en", "Ill-formated subscription-parameter");
            } else if(entry.getValue().length == 0){
                ServiceUtilities.throwResourceUnknownFault("en", "Subscription-parameter is missing value");
            }

            String subRef = entry.getValue()[0];

            /* The subscriptions is not recognized */
            if(!_publishers.containsKey(subRef)){
                Log.d("SimpleSubscriptionManager", "Subscription not found");
                Log.d("SimpleSubscriptionManager", "Expected: " + subRef);
                ServiceUtilities.throwResourceUnknownFault("en", "Subscription not found.");
            }

            Log.d("SimpleSubscriptionManager", "Removed subscription");
            _publishers.remove(subRef);
            firePublisherRegistrationChanged(subRef, PublisherRegistrationEvent.Type.DESTROYED);
            return new DestroyRegistrationResponse();
        }
        ServiceUtilities.throwResourceUnknownFault("en", "The registration was not found as any parameter" +
                " in the request-uri. Please send a request on the form: " +
                "\"http://urlofthis.domain/webservice/?subscription=subscriptionreference");
        return null;
    }

    @Override
    @WebMethod(exclude = true)
    public SoapForwardingHub quickBuild(String endpointReference) {
        try {
            // Ensure the application server is stopped.
            ApplicationServer.getInstance().stop();

            SoapForwardingHub hub = new SoapForwardingHub();
            _hub = hub;

            // Start the application server with this hub
            ApplicationServer.getInstance().start(hub);

            this.setEndpointReference(endpointReference);

            UnpackingConnector connector = new UnpackingConnector(this);
            hub.registerService(connector);
            _connection = connector;

            return hub;
        } catch (Exception e) {
            throw new RuntimeException("Unable to quickbuild: " + e.getMessage());
        }
    }

    @Override
    @WebMethod(exclude = true)
    public void update() {

    }
}


