//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;

import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.services.eventhandling.SubscriptionEvent;
import org.ntnunotif.wsnu.services.general.ExceptionUtilities;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.general.WsnUtilities;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.PauseFailedFault;
import org.oasis_open.docs.wsn.bw_2.ResumeFailedFault;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "PausableSubscriptionManager")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class SimplePausableSubscriptionManager extends AbstractPausableSubscriptionManager {

    private final HashMap<String, Long> _subscriptions = new HashMap<>();
    private final ArrayList<String> _pausedSubscriptions = new ArrayList<>();

    @Override
    @WebMethod(exclude = true)
    public boolean keyExists(String key) {
        return _subscriptions.containsKey(key);
    }

    @Override
    @WebMethod(exclude = true)
    public void addSubscriber(String endpointReference, long subscriptionEnd) {
        _subscriptions.put(endpointReference, subscriptionEnd);
    }

    @Override
    @WebMethod(exclude = true)
    public void removeSubscriber(String endpointReference) {
        _subscriptions.remove(endpointReference);
    }

    @Override
    @WebMethod(exclude = true)
    public void update() {

        long timeNow = System.currentTimeMillis();
        Log.d("SimpleSubscriptionManager", "Updating");
        synchronized(_subscriptions){
            ArrayList<String> toBeRemoved = new ArrayList<>();
            for(Map.Entry<String, Long> entry : _subscriptions.entrySet()){

                if(_pausedSubscriptions.contains(entry.getKey())){
                    _pausedSubscriptions.remove(entry.getKey());
                }

                /* The subscription is expired */
                if(entry.getValue() > timeNow){
                    toBeRemoved.add(entry.getKey());
                }
            }

            for (String s : toBeRemoved) {
                _subscriptions.remove(s);
            }
        }
    }

    @Override
    @WebResult(name = "ResumeSubscriptionResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "ResumeSubscriptionResponse")
    @WebMethod(operationName = "ResumeSubscription")
    public ResumeSubscriptionResponse resumeSubscription
    (
        @WebParam(partName = "ResumeSubscriptionRequest", name = "ResumeSubscription", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        ResumeSubscription resumeSubscriptionRequest
    )
    throws ResourceUnknownFault, ResumeFailedFault {
        RequestInformation requestInformation = connection.getRequestInformation();

        for (Map.Entry<String, String[]> entry : requestInformation.getParameters().entrySet()) {
            if(!entry.getKey().equals(WsnUtilities.subscriptionString)){
                continue;
            }

            /* If there is not one value, something is wrong, but try the first one*/
            if(entry.getValue().length > 1){
                String subRef = entry.getValue()[0];
                if(!_subscriptions.containsKey(subRef)){
                    if(subscriptionIsPaused(subRef)){
                        Log.d("SimplePausableSubscriptionManager", "Resumed subscription");
                        _pausedSubscriptions.add(subRef);
                        fireSubscriptionChanged(subRef, SubscriptionEvent.Type.RESUME);
                        return new ResumeSubscriptionResponse();
                    } else {
                        ExceptionUtilities.throwResumeFailedFault("en", "Subscription is not paused");
                    }
                }
                ExceptionUtilities.throwResourceUnknownFault("en", "Ill-formated subscription-parameter");
            } else if(entry.getValue().length == 0){
                ExceptionUtilities.throwResumeFailedFault("en", "Subscription-parameter in URL is missing value");
            }

            String subRef = entry.getValue()[0];

            /* The subscriptions is not recognized */
            if(!_subscriptions.containsKey(subRef)){
                Log.d("SimplePausableSubscriptionManager", "Subscription not found");
                Log.d("SimplePausableSubscriptionManager", "Expected: " + subRef);
                ExceptionUtilities.throwResourceUnknownFault("en", "Subscription not found.");
            }

            Log.d("SimplePausableSubscriptionManager", "Paused subscription");
            _subscriptions.remove(subRef);
            fireSubscriptionChanged(subRef, SubscriptionEvent.Type.RESUME);
            return new ResumeSubscriptionResponse();
        }
        ExceptionUtilities.throwResumeFailedFault("en", "The subscription was not found as any parameter" +
                " in the request-uri. Please send a request on the form: " +
                "\"http://urlofthis.domain/webservice/?"+WsnUtilities.subscriptionString+"=subscriptionreference");
        return null;
    }

    @Override
    @WebResult(name = "PauseSubscriptionResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "PauseSubscriptionResponse")
    @WebMethod(operationName = "PauseSubscription")
    public PauseSubscriptionResponse pauseSubscription
    (
        @WebParam(partName = "PauseSubscriptionRequest", name = "PauseSubscription", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        PauseSubscription pauseSubscriptionRequest
    )
    throws ResourceUnknownFault, PauseFailedFault {
        RequestInformation requestInformation = connection.getRequestInformation();

        for (Map.Entry<String, String[]> entry : requestInformation.getParameters().entrySet()) {
            if(!entry.getKey().equals(WsnUtilities.subscriptionString)){
                continue;
            }

            /* If there is not one value, something is wrong, but try the first one*/
            if(entry.getValue().length > 1){
                String subRef = entry.getValue()[0];
                if(!_subscriptions.containsKey(subRef)){
                    if(!subscriptionIsPaused(subRef)){
                        Log.d("SimplePausableSubscriptionManager", "Paused subscription");
                        _pausedSubscriptions.add(subRef);
                        fireSubscriptionChanged(subRef, SubscriptionEvent.Type.PAUSE);
                        return new PauseSubscriptionResponse();
                    } else {
                        ExceptionUtilities.throwPauseFailedFault("en", "Subscription is already paused");
                    }
                }
                ExceptionUtilities.throwResourceUnknownFault("en", "Ill-formated subscription-parameter");
            } else if(entry.getValue().length == 0){
                ExceptionUtilities.throwPauseFailedFault("en", "Subscription-parameter in URL is missing value");
            }

            String subRef = entry.getValue()[0];

            /* The subscriptions is not recognized */
            if(!_subscriptions.containsKey(subRef)){
                Log.d("SimplePausableSubscriptionManager", "Subscription not found");
                Log.d("SimplePausableSubscriptionManager", "Expected: " + subRef);
                ExceptionUtilities.throwResourceUnknownFault("en", "Subscription not found.");
            }

            Log.d("SimplePausableSubscriptionManager", "Paused subscription");
            _subscriptions.remove(subRef);
            fireSubscriptionChanged(subRef, SubscriptionEvent.Type.PAUSE);
            return new PauseSubscriptionResponse();
        }
        ExceptionUtilities.throwPauseFailedFault("en", "The subscription was not found as any parameter" +
                " in the request-uri. Please send a request on the form: " +
                "\"http://urlofthis.domain/webservice/?"+WsnUtilities.subscriptionString+"=subscriptionreference");
        return null;
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
    public UnsubscribeResponse unsubscribe
    (
        @WebParam(partName = "UnsubscribeRequest", name = "Unsubscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        Unsubscribe unsubscribeRequest
    )
    throws ResourceUnknownFault, UnableToDestroySubscriptionFault {
        Log.d("SimpleSubscriptionManager", "Received unsubscribe request");
        RequestInformation requestInformation = connection.getRequestInformation();

        for(Map.Entry<String, String[]> entry : requestInformation.getParameters().entrySet()){
            if(!entry.getKey().equals(WsnUtilities.subscriptionString)){
                continue;
            }

            /* If there is not one value, something is wrong, but try the first one*/
            if(entry.getValue().length > 1){
                String subRef = entry.getValue()[0];
                if(_subscriptions.containsKey(subRef)){
                    _subscriptions.remove(subRef);
                    fireSubscriptionChanged(subRef, SubscriptionEvent.Type.UNSUBSCRIBE);
                    return new UnsubscribeResponse();
                } else {
                    ExceptionUtilities.throwResourceUnknownFault("en", "Ill-formated subscription-parameter");
                }
            } else if(entry.getValue().length == 0){
                ExceptionUtilities.throwUnableToDestroySubscriptionFault("en", "Subscription-parameter in URL is missing value");
            }

            String subRef = entry.getValue()[0];

            /* The subscriptions is not recognized */
            if(!_subscriptions.containsKey(subRef)){
                Log.d("SimpleSubscriptionManager", "Subscription not found");
                Log.d("SimpleSubscriptionManager", "Expected: " + subRef);
                ExceptionUtilities.throwResourceUnknownFault("en", "Subscription not found.");
            }

            Log.d("SimpleSubscriptionManager", "Removed subscription");
            _subscriptions.remove(subRef);
            fireSubscriptionChanged(subRef, SubscriptionEvent.Type.UNSUBSCRIBE);
            return new UnsubscribeResponse();
        }

        ExceptionUtilities.throwUnableToDestroySubscriptionFault("en", "The subscription was not found as any parameter" +
                " in the request-uri. Please send a request on the form: " +
                "\"http://urlofthis.domain/webservice/?"+WsnUtilities.subscriptionString+"=subscriptionreference");
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
            if (!entry.getKey().equals(WsnUtilities.subscriptionString)) {
                continue;
            }
            Log.d("SimpleSubscriptionManager", "Found subscription parameter");

            /* There is not one value, something is wrong, but try the first one */
            if(entry.getValue().length >= 1){
                String subRef = entry.getValue()[0];

                if(!_subscriptions.containsKey(subRef)){
                    ExceptionUtilities.throwResourceUnknownFault("en", "Given resource was unknown: " + subRef);
                }
            /* We just continue down here as the time-fetching operations are rather large */
            }else if(entry.getValue().length == 0){
                ExceptionUtilities.throwResourceUnknownFault("en", "A blank resource is always unknown.");
            }

            String subRef = entry.getValue()[0];

            long time = ServiceUtilities.interpretTerminationTime(renewRequest.getTerminationTime());

            if(time < System.currentTimeMillis()) {
                ExceptionUtilities.throwUnacceptableTerminationTimeFault("en", "Tried to renew a subscription so it " +
                        "should last until a time that has already passed.");
            }

            Log.d("SimpleSubscriptionManager", "Successfully renewed subscription");
            _subscriptions.put(subRef, time);
            fireSubscriptionChanged(subRef, SubscriptionEvent.Type.RENEW);
            return new RenewResponse();
        }

        Log.d("SimpleSubscriptionManager", "Subscription not found, probably ill-formatted request");
        ExceptionUtilities.throwResourceUnknownFault("en", "The subscription was not found as any parameter" +
                " in the request-uri. Please send a request on the form: " +
                "\"http://urlofthis.domain/webservice/?"+WsnUtilities.subscriptionString+"=subscriptionreference");
        return null;
    }

    @Override
    @WebMethod(exclude = true)
    public boolean subscriptionIsPaused(String subscriptionReference) {
        return _subscriptions.containsKey(subscriptionReference) && _pausedSubscriptions.contains(subscriptionReference);
    }

    @Override
    @WebMethod(exclude = true)
    public boolean hasSubscription(String subscriptionReference) {
        return _subscriptions.containsKey(subscriptionReference);
    }
}
