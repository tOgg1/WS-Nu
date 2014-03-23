package org.ntnunotif.wsnu.services.general;

import org.w3._2001._12.soap_envelope.Envelope;

import javax.jws.WebMethod;

/**
 * Created by tormod on 23.03.14.
 */
public interface WebService {

    @WebMethod(operationName="acceptSoapMessage")
    public void acceptSoapMessage(Envelope envelope);
}
