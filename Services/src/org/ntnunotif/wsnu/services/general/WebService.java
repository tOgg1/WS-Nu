package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.WebServiceConnector;
import org.ntnunotif.wsnu.base.util.EndpointReference;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.activation.UnsupportedDataTypeException;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The parent of all web services implemented in this module. The main functionality of this class is storing an _endpointReference,
 * holding a reference to the hub (must be supplemented in any implementation class' constructor) as well as some utility methods.
 * @author Tormod Haugland
 * Created by tormod on 23.03.14.
 */
@javax.jws.WebService
public abstract class WebService {

    /**
     * Reference to the connected hub
     */
    protected Hub _hub;

    protected WebService(){

    }

    protected WebService(Hub _hub) {
        this._hub = _hub;
    }

    @EndpointReference
    protected String endpointReference;

    public String getEndpointReference() {
        return endpointReference;
    }

    public void setEndpointReference(String endpointReference) {
        this.endpointReference = _hub.getInetAdress() + "/" + endpointReference;
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
                    System.out.println(types[i + 1] + " | |  " + args[i].getClass());
                    System.out.println(!types[i + 1].isAssignableFrom(args[i].getClass()));
                    if (!types[i + 1].isAssignableFrom(args[i].getClass())) {
                        continue outer;
                    }
                    System.out.println("Hello");
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
            this._hub = hub;
            return hub;
        }catch(InvocationTargetException e){
            Throwable t = e.getCause();
            t.printStackTrace();
            hub.stop();
            throw new RuntimeException("Unable to quickbuild: " + t.getMessage());
        }catch (Exception e){
            hub.stop();
            throw new RuntimeException("Unable to quickbuild: " + e.getMessage());
        }
    }
}
