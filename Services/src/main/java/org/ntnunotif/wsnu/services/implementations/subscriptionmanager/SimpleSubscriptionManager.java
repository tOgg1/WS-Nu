package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionEvent;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.ArrayList;
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
     * Empty constructor doing nothing.
     */
    public SimpleSubscriptionManager() {
        _subscriptions = new HashMap<>();
    }

    /**
     * Constructor taking a hub as an argument
     * @param hub
     */
    public SimpleSubscriptionManager(Hub hub) {
        super(hub);
        _subscriptions = new HashMap<>();
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
            ArrayList<String> toBeRemoved = new ArrayList<>();
            for(Map.Entry<String, Long> entry : _subscriptions.entrySet()){

                /* The subscription is expired */
                if(entry.getValue().longValue() > timeNow){
                    toBeRemoved.add(entry.getKey());
                }
            }

            for (String s : toBeRemoved) {
                _subscriptions.remove(s);
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
        RequestInformation requestInformation = connection.getRequestInformation();

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
                ServiceUtilities.throwResourceUnknownFault("en", "Ill-formated subscription-parameter");
            } else if(entry.getValue().length == 0){
                ServiceUtilities.throwUnableToDestroySubscriptionFault("en", "Subscription-parameter in URL is missing value");
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
            fireSubscriptionChanged(subRef, SubscriptionEvent.Type.UNSUBSCRIBE);
            return new UnsubscribeResponse();
        }

        ServiceUtilities.throwResourceUnknownFault("en", "The subscription was not found as any parameter" +
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

        RequestInformation requestInformation = connection.getRequestInformation();

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
            fireSubscriptionChanged(subRef, SubscriptionEvent.Type.RENEW);
            return new RenewResponse();
        }

        Log.d("SimpleSubscriptionManager", "Subscription not found, probably ill-formatted request");
        ServiceUtilities.throwResourceUnknownFault("en", "The resource was not found. The request was probably ill " +
                "formatted");
        return null;
    }

    @Override
    public boolean hasSubscription(String subscriptionReference) {
        return _subscriptions.containsKey(subscriptionReference);
    }
}
