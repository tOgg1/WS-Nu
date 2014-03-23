package org.ntnunotif.wsnu.services.subscriptionmanager;

import org.ntnunotif.wsnu.base.util.EndpointParam;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple subscription manager that stores subscriptions in a HashMap
 * Created by tormod on 3/19/14.
 */
public class SimpleSubscriptionManager extends AbstractSubscriptionManager {

    private HashMap<String, Long> _subscriptionTimes;
    private boolean _autoRenew = false;

    /**
     * Time period added to renew if no particular period is specified. Default value is one day.
     */
    private final long renewTime = 86400;

    public SimpleSubscriptionManager() {
    }

    public void setRenew(boolean autoRenew){
        _autoRenew = autoRenew;
    }

    @Override
    public void addSubscriber(String endpointReference, long subscriptionEnd) {
        _subscriptionTimes.put(endpointReference, subscriptionEnd);
    }

    @Override
    public void removeSubscriber(String endpointReference) {
        _subscriptionTimes.remove(endpointReference);
    }

    @Override
    public void update() {
        long timeNow = System.currentTimeMillis();

        synchronized (_subscriptionTimes){
            for(Map.Entry<String, Long> entry : _subscriptionTimes.entrySet()){

                /* The subscription is expired */
                if(entry.getValue().longValue() > timeNow){
                    if(_autoRenew){
                        entry.setValue(entry.getValue().longValue() + renewTime);
                    }else{
                        _subscriptionTimes.remove(entry.getKey());
                    }
                }
            }
        }
    }

    @Override
    @WebMethod(operationName = "Unsubscribe")
    public UnsubscribeResponse unsubscribe(@WebParam(partName = "UnsubscribeRequest", name = "Unsubscribe",
                                                     targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                                           Unsubscribe unsubscribeRequest, @EndpointParam String endpointReference)
                                           throws ResourceUnknownFault, UnableToDestroySubscriptionFault {
        System.out.println(unsubscribeRequest);
        System.out.println(endpointReference);
        return null;
    }

    @Override
    @WebMethod(operationName = "Renew")
    public RenewResponse renew(@WebParam(partName = "RenewRequest", name = "Renew",
                                         targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
                               Renew renewRequest, @EndpointParam String endpointReference)
                               throws ResourceUnknownFault, UnacceptableTerminationTimeFault {
        return null;
    }
}
