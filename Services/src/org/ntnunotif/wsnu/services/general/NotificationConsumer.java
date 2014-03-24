package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.util.RequestInformation;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author Tormod Haugland
 * Created by tormod on 23.03.14.
 */
@javax.jws.WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "SimpleConsumer")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface NotificationConsumer {

    @Oneway
    @WebMethod(operationName = "Notify")
    public void notify(
            @WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
            org.oasis_open.docs.wsn.b_2.Notify notify, RequestInformation requestInformation
    );

}
