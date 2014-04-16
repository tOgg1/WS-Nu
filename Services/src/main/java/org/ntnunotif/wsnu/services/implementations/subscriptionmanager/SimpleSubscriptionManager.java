package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple subscription manager that stores subscriptions in a HashMap
 * Created by tormod on 3/19/14.
 */
@WebService
public class SimpleSubscriptionManager extends AbstractSubscriptionManager {

    /**
     * Hashmap of subscriptions
     */
    private HashMap<String, Long> _subscriptions;

    /**
     * Variable indicating whether the subscription manager should autorenew or not.
     */
    private boolean _autoRenew = false;

    /**
     * Time period added to renew if no particular period is specified. Default value is one day.
     */
    private final long renewTime = 86400;

    /**
     * Constructor.
     * @param hub
     */
    public SimpleSubscriptionManager(Hub hub) {
        super(hub);
        _subscriptions = new HashMap<String, Long>();
    }


    @WebMethod(exclude = true)
    public void setAutoRenew(boolean autoRenew){
        _autoRenew = autoRenew;
    }

    @Override
    @WebMethod(exclude = true)
    public boolean keyExists(String key) {
        return _subscriptions.containsKey(key);
    }

    @Override
    @WebMethod(exclude = true)
    public void addSubscriber(String subscriptionReference, long subscriptionEnd) {
        Log.d("SimpleSubscriptionmanager", "Adding subscription: " + subscriptionReference);
        _subscriptions.put(subscriptionReference, subscriptionEnd);
        System.out.println(_subscriptions.size());
    }

    @Override
    @WebMethod(exclude = true)
    public void removeSubscriber(String subscriptionReference) {
        _subscriptions.remove(subscriptionReference);
    }

    @Override
    @WebMethod(exclude = true)
    public void update() {
        long timeNow = System.currentTimeMillis();
        Log.d("SimpleSubscriptionManager", "Updating");
        synchronized(_subscriptions){
            for(Map.Entry<String, Long> entry : _subscriptions.entrySet()){

                /* The subscription is expired */
                if(entry.getValue().longValue() > timeNow){
                    if(_autoRenew){
                        entry.setValue(entry.getValue().longValue() + renewTime);
                    }else{
                        _subscriptions.remove(entry.getKey());
                    }
                }
            }
        }
    }

    /**
     * SimpleSubscriptionManagers implementation of unsubscribe.
     * @param unsubscribeRequest
     * @return
     * @throws ResourceUnknownFault
     * @throws UnableToDestroySubscriptionFault
     */
    @Override
    @WebResult(name = "UnsubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "UnsubscribeResponse")
    @WebMethod(operationName = "Unsubscribe")
    public UnsubscribeResponse unsubscribe(
           @WebParam(partName = "UnsubscribeRequest", name = "Unsubscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
           Unsubscribe unsubscribeRequest)
    throws ResourceUnknownFault, UnableToDestroySubscriptionFault {
        Log.d("SimpleSubscriptionManager", "Received unsubscribe request");
        RequestInformation requestInformation = _connection.getRequestInformation();

        for(Map.Entry<String, String[]> entry : requestInformation.getParameters().entrySet()){
            if(!entry.getKey().equals("subscription")){
                continue;
            }

            /* If there is not one value, something is wrong, but try the first one*/
            if(entry.getValue().length > 1){
                String subRef = entry.getValue()[0];
                if(!_subscriptions.containsKey(subRef)){
                    _subscriptions.remove(subRef);
                    return new UnsubscribeResponse();
                }
                throw new ResourceUnknownFault("Ill-formated subscription-parameter", new ResourceUnknownFaultType());
            } else if(entry.getValue().length == 0){
                throw new UnableToDestroySubscriptionFault("Subscription-parameter is missing value", new UnableToDestroySubscriptionFaultType());
            }

            String subRef = entry.getValue()[0];

            /* The subscriptions is not recognized */
            if(!_subscriptions.containsKey(subRef)){
                Log.d("SimpleSubscriptionManager", "Subscription not found");
                Log.d("SimpleSubscriptionManager", "Expected: " + subRef);
                ServiceUtilities.throwResourceUnknownFault("en", "Subscription not found.");
            }

            Log.d("SimpleSubscriptionManager", "Removed subscription");
            _subscriptions.remove(subRef);
            return new UnsubscribeResponse();
        }

        ServiceUtilities.throwUnableToDestroySubscriptionFault("en", "The subscription was not found as any parameter" +
                " in the request-uri. Please send a request on the form: " +
                "\"http://urlofthis.domain/webservice/?subscription=subscriptionreference");
        return null;
    }

    /**
     * SimpleSubscriptionManager's implementation of renew.
     * @param renewRequest
     * @return
     * @throws ResourceUnknownFault
     * @throws UnacceptableTerminationTimeFault
     */
    @Override
    @WebResult(name = "RenewResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "RenewResponse")
    @WebMethod(operationName = "Renew")
    public RenewResponse renew(
        @WebParam(partName = "RenewRequest", name = "Renew", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        Renew renewRequest)
    throws ResourceUnknownFault, UnacceptableTerminationTimeFault {

        System.out.println(_subscriptions.size());

        RequestInformation requestInformation = _connection.getRequestInformation();

        Log.d("SimpleSubscriptionManager", "Received renew request");
        /* Find the subscription tag */
        for(Map.Entry<String, String[]> entry : requestInformation.getParameters().entrySet()) {
            if (!entry.getKey().equals("subscription")) {
                continue;
            }
            Log.d("SimpleSubscriptionManager", "Found subscription parameter");

            /* The is not one value, something is wrong, but try the first one */
            if(entry.getValue().length >= 1){
                String subRef = entry.getValue()[0];

                if(!_subscriptions.containsKey(subRef)){
                    Log.d("SimpleSubscriptionManager", "Subscription not found");
                    Log.d("SimpleSubscriptionManager", "All subscriptions:");
                    for (String s : _subscriptions.keySet()) {
                        Log.d("SimpleSubscriptionmanager", s);
                    }
                    Log.d("SimpleSubscriptionManager", "Expected: " + subRef);
                    ServiceUtilities.throwResourceUnknownFault("en", "Given resource was unknown: " + subRef);
                }
            /* We just continue down here as the time-fetching operations are rather large */
            }else if(entry.getValue().length == 0){
                ServiceUtilities.throwResourceUnknownFault("en", "A blank resource is always unknown.");
            }

            String subRef = entry.getValue()[0];

            long time = ServiceUtilities.interpretTerminationTime(renewRequest.getTerminationTime());

            if(time < System.currentTimeMillis()) {
                ServiceUtilities.throwUnacceptableTerminationTimeFault("en", "Tried to renew a subscription so it " +
                        "should last until a time that has already passed.");
            }

            Log.d("SimpleSubscriptionManager", "Successfully renewed subscription");
            _subscriptions.put(subRef, time);
            return new RenewResponse();
        }

        Log.d("SimpleSubscriptionManager", "Subscription not found, probably ill-formatted request");
        ServiceUtilities.throwResourceUnknownFault("en", "The resource was not found. The request was probably ill " +
                "formatted");
        return null;
    }

    //@Override
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
}
