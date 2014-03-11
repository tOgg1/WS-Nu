package org.ntnunotif.wsnu.services.SubscriptionManager;

import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.jws.WebParam;

/**
 * Created by tormod on 3/11/14.
 */
public class SubscriptionManager implements org.oasis_open.docs.wsn.bw_2.SubscriptionManager{

    @Override
    public UnsubscribeResponse unsubscribe(@WebParam(partName = "UnsubscribeRequest", name = "Unsubscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Unsubscribe unsubscribeRequest) throws ResourceUnknownFault, UnableToDestroySubscriptionFault {
        return null;
    }

    @Override
    public RenewResponse renew(@WebParam(partName = "RenewRequest", name = "Renew", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Renew renewRequest) throws ResourceUnknownFault, UnacceptableTerminationTimeFault {
        return null;
    }
}
