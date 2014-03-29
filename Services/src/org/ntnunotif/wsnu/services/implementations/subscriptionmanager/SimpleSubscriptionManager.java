package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;

import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.UnpackingRequestInformationConnector;
import org.ntnunotif.wsnu.base.util.Information;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3._2001._12.soap_envelope.Envelope;

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

    @Override
    public Object acceptSoapMessage(@WebParam Envelope envelope, @Information RequestInformation requestInformation) {
        return null;
    }

    @Override
    public InternalMessage acceptRequest(@Information RequestInformation requestInformation) {
        return null;
    }

    public void setAutoRenew(boolean autoRenew){
        _autoRenew = autoRenew;
    }

    @Override
    public boolean keyExists(String key) {
        return _subscriptions.containsKey(key);
    }

    @Override
    public void addSubscriber(String subscriptionReference, long subscriptionEnd) {
        Log.d("SimpleSubscriptionmanager", "Adding subscription: " + subscriptionReference);
        _subscriptions.put(subscriptionReference, subscriptionEnd);
        System.out.println(_subscriptions.size());
    }

    @Override
    public void removeSubscriber(String subscriptionReference) {
        _subscriptions.remove(subscriptionReference);
    }

    @Override
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
     * @param requestInformation
     * @return
     * @throws ResourceUnknownFault
     * @throws UnableToDestroySubscriptionFault
     */
    @Override
    @WebResult(name = "UnsubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "UnsubscribeResponse")
    @WebMethod(operationName = "Unsubscribe")
    public UnsubscribeResponse unsubscribe(
           @WebParam(partName = "UnsubscribeRequest", name = "Unsubscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
           Unsubscribe unsubscribeRequest, @Information RequestInformation requestInformation)
    throws ResourceUnknownFault, UnableToDestroySubscriptionFault {
        System.out.println(_subscriptions.size());
        Log.d("SimpleSubscriptionManager", "Received unsubscribe request");
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
                throw new ResourceUnknownFault();
            } else if(entry.getValue().length == 0){
                throw new UnableToDestroySubscriptionFault();
            }

            String subRef = entry.getValue()[0];

            /* The subscriptions is not recognized */
            if(!_subscriptions.containsKey(subRef)){
                Log.d("SimpleSubscriptionManager", "Subscription not found");
                Log.d("SimpleSubscriptionManager", "Subscription not found");
                Log.d("SimpleSubscriptionManager", "All subscriptions:");
                for (String s : _subscriptions.keySet()) {
                    Log.d("SimpleSubscriptionmanager", s);
                }
                Log.d("SimpleSubscriptionManager", "Expected: " + subRef);
                throw new ResourceUnknownFault();
            }

            Log.d("SimpleSubscriptionManager", "Removed subscription");
            _subscriptions.remove(subRef);
            return new UnsubscribeResponse();
        }

        throw new UnableToDestroySubscriptionFault();
    }

    /**
     * SimpleSubscriptionManager's implementation of renew.
     * @param renewRequest
     * @param requestInformation
     * @return
     * @throws ResourceUnknownFault
     * @throws UnacceptableTerminationTimeFault
     */
    @Override
    @WebResult(name = "RenewResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "RenewResponse")
    @WebMethod(operationName = "Renew")
    public RenewResponse renew(
        @WebParam(partName = "RenewRequest", name = "Renew", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        Renew renewRequest, @Information RequestInformation requestInformation)
    throws ResourceUnknownFault, UnacceptableTerminationTimeFault {

        System.out.println(_subscriptions.size());
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
                    throw new ResourceUnknownFault();
                }
            /* We just continue down here as the time-fetching operations are rather large */
            }else if(entry.getValue().length == 0){
                throw new ResourceUnknownFault();
            }

            String subRef = entry.getValue()[0];

            long time = ServiceUtilities.interpretTerminationTime(renewRequest.getTerminationTime());

            if(time < System.currentTimeMillis()) {
                throw new UnacceptableTerminationTimeFault();
            }

            Log.d("SimpleSubscriptionManager", "Succesfully renewed subscription");
            _subscriptions.put(subRef, time);
            return new RenewResponse();
        }

        Log.d("SimpleSubscriptionManager", "Subscription not found, probably ill-formatted request");
        throw new ResourceUnknownFault();
    }

    @Override
    public ForwardingHub quickBuild() {
        try {
            ForwardingHub hub = new ForwardingHub();
            UnpackingRequestInformationConnector connector = new UnpackingRequestInformationConnector(this);
            hub.registerService(connector);
            this.registerConnection(connector);
            _hub = hub;
            return hub;
        } catch (Exception e) {
            throw new RuntimeException("Unable to quickbuild: " + e.getMessage());
        }
    }
}
