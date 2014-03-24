package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.Information;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Simple subscription manager that stores subscriptions in a HashMap
 * Created by tormod on 3/19/14.
 */
public class SimpleSubscriptionManager extends AbstractSubscriptionManager {

    private HashMap<String, Long> _subscriptions;
    private boolean _autoRenew = false;

    /**
     * Time period added to renew if no particular period is specified. Default value is one day.
     */
    private final long renewTime = 86400;

    public SimpleSubscriptionManager(Hub hub) {
        super(hub);
        _subscriptions = new HashMap<String, Long>();
    }

    public void setAutoRenew(boolean autoRenew){
        _autoRenew = autoRenew;
    }

    @Override
    public boolean keyExists(String key) {
        return false;
    }

    @Override
    public void addSubscriber(String subscriptionReference, long subscriptionEnd) {
        _subscriptions.put(subscriptionReference, subscriptionEnd);
    }

    @Override
    public void removeSubscriber(String subscriptionReference) {
        _subscriptions.remove(subscriptionReference);
    }

    @Override
    public void update() {
        long timeNow = System.currentTimeMillis();

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

    @Override
    @WebResult(name = "UnsubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "UnsubscribeResponse")
    @WebMethod(operationName = "Unsubscribe")
    public UnsubscribeResponse unsubscribe(
            @WebParam(partName = "UnsubscribeRequest", name = "Unsubscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
           Unsubscribe unsubscribeRequest, @Information RequestInformation requestInformation)
    throws ResourceUnknownFault, UnableToDestroySubscriptionFault {
        Log.d("SimpleSubscriptionManager", "RequestInformation:\nEndpointReference:  " + requestInformation.getEndpointReference()
                                            + "\nNamespaceContext: " + requestInformation.getNamespaceContext()
                                            + "\nQuery: " + requestInformation.getRequestURL());

        for(Map.Entry<String, String[]> entry : requestInformation.getParameters().entrySet()){
            System.out.println("Key:" +  entry.getKey());
            for(String s : entry.getValue()){
                System.out.print("Value: " + s);
            }
        }

        return new UnsubscribeResponse();
    }

    @Override
    @WebResult(name = "RenewResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "RenewResponse")
    @WebMethod(operationName = "Renew")
    public RenewResponse renew(
        @WebParam(partName = "RenewRequest", name = "Renew", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        Renew renewRequest, @Information RequestInformation requestInformation)
    throws ResourceUnknownFault, UnacceptableTerminationTimeFault {
        return new RenewResponse();
    }

    @Override
    public void acceptSoapMessage(@WebParam Envelope envelope) {

    }

    @Override
    public Hub quickBuild() {
        return null;
    }
}
