package org.oasis_open.docs.wsn.bw_2;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was org.generated by Apache CXF 2.7.10
 * 2014-03-03T11:43:04+01:00
 * Generated source version: 2.7.10
 * 
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "PausableSubscriptionManager")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface PausableSubscriptionManager {

    @WebResult(name = "UnsubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "UnsubscribeResponse")
    @WebMethod(operationName = "Unsubscribe")
    public org.oasis_open.docs.wsn.b_2.UnsubscribeResponse unsubscribe(
        @WebParam(partName = "UnsubscribeRequest", name = "Unsubscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        org.oasis_open.docs.wsn.b_2.Unsubscribe unsubscribeRequest
    ) throws org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, UnableToDestroySubscriptionFault;

    @WebResult(name = "RenewResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "RenewResponse")
    @WebMethod(operationName = "Renew")
    public org.oasis_open.docs.wsn.b_2.RenewResponse renew(
        @WebParam(partName = "RenewRequest", name = "Renew", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        org.oasis_open.docs.wsn.b_2.Renew renewRequest
    ) throws org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, UnacceptableTerminationTimeFault;

    @WebResult(name = "ResumeSubscriptionResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "ResumeSubscriptionResponse")
    @WebMethod(operationName = "ResumeSubscription")
    public org.oasis_open.docs.wsn.b_2.ResumeSubscriptionResponse resumeSubscription(
        @WebParam(partName = "ResumeSubscriptionRequest", name = "ResumeSubscription", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        org.oasis_open.docs.wsn.b_2.ResumeSubscription resumeSubscriptionRequest
    ) throws org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, ResumeFailedFault;

    @WebResult(name = "PauseSubscriptionResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "PauseSubscriptionResponse")
    @WebMethod(operationName = "PauseSubscription")
    public org.oasis_open.docs.wsn.b_2.PauseSubscriptionResponse pauseSubscription(
        @WebParam(partName = "PauseSubscriptionRequest", name = "PauseSubscription", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        org.oasis_open.docs.wsn.b_2.PauseSubscription pauseSubscriptionRequest
    ) throws org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, PauseFailedFault;
}
