package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.WebServiceConnector;
import org.ntnunotif.wsnu.base.util.EndpointReference;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.activation.UnsupportedDataTypeException;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 * The parent of all web services implemented in this module. The main functionality of this class is storing an endpointReference,
 * holding a reference to the hub (must be supplemented in any implementation class' constructor) as well as some utility methods.
 * @author Tormod Haugland
 * Created by tormod on 23.03.14.
 */
public abstract class WebService {

    /**
     * Reference to the connected hub
     */
    protected Hub _hub;

    protected WebService(){};

    protected WebService(Hub _hub) {
        this._hub = _hub;
    }

    @EndpointReference
    protected String endpointReference;

    public String getEndpointReference() {
        return endpointReference;
    }

    public void setEndpointReference(String endpointReference) {
        this.endpointReference = endpointReference;
    }

    public Hub getHub() {
        return _hub;
    }

    public void setHub(Hub _hub) {
        this._hub = _hub;
    }

    @WebMethod(operationName="acceptSoapMessage")
    public abstract Object acceptSoapMessage(@WebParam Envelope envelope);

    public abstract Hub quickBuild();
    public abstract Hub quickBuild(Class<? extends WebServiceConnector> connectorClass, Object... args) throws UnsupportedDataTypeException;

}
