package org.ntnunotif.wsnu.services.interfaces;

import org.ntnunotif.wsnu.base.util.EndpointParam;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Created by tormod on 23.03.14.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "SubscriptionManager")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface SubscriptionManager {

    @WebResult(name = "UnsubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "UnsubscribeResponse")
    @WebMethod(operationName = "Unsubscribe")
    public org.oasis_open.docs.wsn.b_2.UnsubscribeResponse unsubscribe(
            @WebParam(partName = "UnsubscribeRequest", name = "Unsubscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
            org.oasis_open.docs.wsn.b_2.Unsubscribe unsubscribeRequest, @EndpointParam String endpointReference
    ) throws org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, UnableToDestroySubscriptionFault;

    @WebResult(name = "RenewResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "RenewResponse")
    @WebMethod(operationName = "Renew")
    public org.oasis_open.docs.wsn.b_2.RenewResponse renew(
            @WebParam(partName = "RenewRequest", name = "Renew", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
            org.oasis_open.docs.wsn.b_2.Renew renewRequest, @EndpointParam String endpointReference
    ) throws org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, UnacceptableTerminationTimeFault;
}
