package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.ServiceConnection;
import org.ntnunotif.wsnu.base.internal.WebServiceConnector;
import org.ntnunotif.wsnu.base.util.EndpointReference;
import org.ntnunotif.wsnu.base.util.Information;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.activation.UnsupportedDataTypeException;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * The parent of all web services implemented in this module. The main functionality of this class is storing an _endpointReference,
 * holding a reference to the hub (must be supplemented in any implementation class' constructor) as well as some utility methods.
 * @author Tormod Haugland
 * Created by tormod on 23.03.14.
 */
@javax.jws.WebService
public abstract class WebService {

    private ArrayList<ServiceConnection> _connections;

    /**
     * Factory available to all WebServices
     */
    public ObjectFactory factory = new ObjectFactory();

    /**
     * Reference to the connected hub
     */
    protected Hub _hub;

    /**
     * Default constructor.
     */
    protected WebService(){
        _connections = new ArrayList<>();
    }

    /**
     * Constructor taking a hub as a parameter.
     * @param _hub
     */
    protected WebService(Hub _hub) {
        this._hub = _hub;
        _connections = new ArrayList<>();
    }

    /**
     * The actual endpointreference of this Web Service.
     */
    @EndpointReference
    protected String endpointReference;

    /**
     * Retrieves the endpointreference of the Web Service.
     * @return
     */
    public String getEndpointReference() {
        return endpointReference;
    }

    /**
     * Sets the endpointreference of this hub. Note that this function fetches the Applicationserver's listening IP
     * (if several IP's are listed, the applicationserver chooses which to return. If you need a specific one, please call {@link } {@link #forceEndpointReference(String)} ) and appends the endpointreference to it.
     * Thus, if you want to give a web service the endpoint reference http://serversurl.domain/myWebService,
     * you only have to pass in "myWebService".
     * @param endpointReference
     */
    public void setEndpointReference(String endpointReference) {
        if(endpointReference.contains("\\")){
            throw new IllegalArgumentException("EndpointReference can not containt the character \\(backslash)");
        }

        this.endpointReference = _hub.getInetAdress() + "/" + endpointReference;

        for(ServiceConnection connection : _connections){
            connection.endpointUpdated(this.endpointReference);
        }
    }

    /**
     * Forces the endpoint reference to the endpointreference set.
     * @param endpointReference
     */
    public void forceEndpointReference(String endpointReference){
        this.endpointReference = endpointReference;
        for(ServiceConnection connection : _connections){
            connection.endpointUpdated(this.endpointReference);
        }
    }

    /**
     * Register a connector forwarding to this web service.
     * @param connection
     */
    protected void registerConnection(ServiceConnection connection){
        _connections.add(connection);
    }

    /**
     * Unregister a connector forwarding to this web service.
     * @param connection
     */
    protected void unregisterConnection(ServiceConnection connection){
        _connections.remove(connection);
    }

    /**
     * Setter for the hub. Does nothing special.
     * @return
     */
    public Hub getHub() {
        return _hub;
    }

    /**
     * Getter for the hub. Does nothing special.
     * @param _hub
     */
    public void setHub(Hub _hub) {
        this._hub = _hub;
    }

    /**
     * The abstract method that accepts a soap message.
     * @param envelope
     * @param requestInformation
     * @return
     */
    @WebMethod(operationName="AcceptSoapMessage")
    public abstract Object acceptSoapMessage(@WebParam Envelope envelope, @Information RequestInformation requestInformation);

    /**
     * The default AcceptRequest method of a WebService. This handles requests by looking for matching files, and nothing more.
     * If specific requests, in particular with parameters, needs to be handled, this method should then be overrided.
     * @param requestInformation
     * @return
     */
    @WebMethod(operationName="AcceptRequest")
    public InternalMessage acceptRequest(@Information RequestInformation requestInformation){
        String uri = requestInformation.getRequestURL();
        Map<String, String[]> parameters = requestInformation.getParameters();

        if(!uri.matches("^"+getEndpointReference())){
            return new InternalMessage(STATUS_FAULT|STATUS_INVALID_DESTINATION, null);
        }

        try{
            FileInputStream stream = new FileInputStream(uri.replaceAll("^"+getEndpointReference(), ""));
            InternalMessage returnMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_MESSAGE_IS_INPUTSTREAM, stream);
            return returnMessage;
        }catch(FileNotFoundException e){
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_NOT_FOUND, null);
        }
    }

    /**
     * Quickbuilds an implementing Web Service.
     * @return The hub connected to the built Web Service
     */
    public abstract Hub quickBuild();

    /**
     * Quickbuilds a Web Service. This function takes as arguments the class of the connector and the arguments to passed
     * to it's constructor. This function catches {@link java.lang.reflect.InvocationTargetException} and any other {@link java.lang.Exception}.
     * By doing this it it stops the hub from constructing and throws an IllegalArgumentException.
     * @param connectorClass
     * @param args
     * @return
     * @throws UnsupportedDataTypeException
     */
    public Hub quickBuild(Class<? extends WebServiceConnector> connectorClass, Object... args) throws UnsupportedDataTypeException {
        ForwardingHub hub = null;
        try {
            hub = new ForwardingHub();
        } catch (Exception e) {
            hub.stop();
        }

        try {
            Constructor[] constructors = connectorClass.getConstructors();
            Constructor relevantConstructor = null;

            outer:
            for (Constructor constructor : constructors) {
                Class<?>[] types = constructor.getParameterTypes();
                if (types.length != args.length + 1) {
                    continue;
                }

                for (int i = 0; i < args.length; i++) {
                    if (!types[i + 1].isAssignableFrom(args[i].getClass())) {
                        continue outer;
                    }
                }
                relevantConstructor = constructor;
                break;
            }

            Object[] newArgs = new Object[args.length + 1];
            newArgs[0] = this;

            for (int i = 0; i < args.length; i++) {
                newArgs[i + 1] = args[i];
            }

            WebServiceConnector connector = (WebServiceConnector) relevantConstructor.newInstance(newArgs);
            hub.registerService(connector);
            this.registerConnection(connector);
            _hub = hub;
            return hub;
        }catch(InvocationTargetException e){
            Throwable t = e.getCause();
            t.printStackTrace();
            hub.stop();
            throw new IllegalArgumentException("Unable to quickbuild: " + t.getMessage());
        }catch (Exception e){
            hub.stop();
            throw new IllegalArgumentException("Unable to quickbuild: " + e.getMessage());
        }
    }
}
