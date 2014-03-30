package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.ServiceConnection;
import org.ntnunotif.wsnu.base.internal.SoapUnpackingHub;
import org.ntnunotif.wsnu.base.internal.WebServiceConnector;
import org.ntnunotif.wsnu.base.util.*;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;

import javax.activation.UnsupportedDataTypeException;
import javax.jws.WebMethod;
import java.io.*;
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
@javax.jws.WebService(name = "GenericWebService")
public abstract class WebService {

    /**
     * Contentmanagers for pure requests.
     */
    protected ArrayList<ServiceUtilities.ContentManager> _contentManagers;

    /**
     * Factory available to all WebServices
     */
    //TODO: This shouldnt be here
    public org.oasis_open.docs.wsn.b_2.ObjectFactory baseFactory = new ObjectFactory();

    /**
     * Reference to the connected hub
     */
    protected Hub _hub;

    /**
     * Default constructor.
     */
    protected WebService(){
    }

    /**
     * Reference to the connection of this Web Service.
     */
    @Connection
    protected ServiceConnection _connection;

    /**
     * Constructor taking a hub as a parameter.
     * @param _hub
     */
    protected WebService(Hub _hub) {
        this._hub = _hub;
    }

    /**
     * The actual endpointreference of this Web Service.
     */
    @EndpointReference
    protected String endpointReference;

    /**
     * The short version of the endpointReference. If the endpoint reference is 133.371.337.133/endpoint,
     * this value would be endpoint
     */
    protected String pureEndpointReference;

    /**
     * Retrieves the endpointreference of the Web Service.
     * @return
     */
    @WebMethod(exclude = true)
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
    @WebMethod(exclude = true)
    public void setEndpointReference(String endpointReference) {
        if(endpointReference.contains("\\")){
            throw new IllegalArgumentException("EndpointReference can not containt the character \\(backslash)");
        }

        this.pureEndpointReference = endpointReference;
        this.endpointReference = _hub.getInetAdress() + "/" + endpointReference;

        _connection.endpointUpdated(this.endpointReference);
    }

    /**
     * Forces the endpoint reference to the endpointreference set.
     * @param endpointReference
     */
    @WebMethod(exclude = true)
    public void forceEndpointReference(String endpointReference){
        this.endpointReference = endpointReference;
        //TODO: Try to filter out the pureEndpointReference
        this.pureEndpointReference = endpointReference;
        if(_connection == null)
            return;
        _connection.endpointUpdated(endpointReference);
    }

    /**
     * Setter for the hub. Does nothing special.
     * @return
     */
    @WebMethod(exclude = true)
    public Hub getHub() {
        return _hub;
    }

    /**
     * Getter for the hub. Does nothing special.
     * @param _hub
     */
    @WebMethod(exclude = true)
    public void setHub(Hub _hub) {
        this._hub = _hub;
    }

    /**
     * The default AcceptRequest method of a WebService. This handles requests by looking for matching files, and nothing more.
     * If specific requests, in particular with parameters, needs to be handled, this method should then be overrided.
     * @return
     */
    @WebMethod(exclude=true)
    public InternalMessage acceptRequest(){
        RequestInformation requestInformation = _connection.getReqeustInformation();

        String uri = requestInformation.getRequestURL();
        Map<String, String[]> parameters = requestInformation.getParameters();

        if(!uri.matches("^/?"+pureEndpointReference+"(.*)?")){
            Log.d("WebService", "URI: " + uri + " does not match this Web Service's endpointreference " + pureEndpointReference+". Discrepancy: " + uri.replaceAll("^/?"+endpointReference+".*?", ""));
            return new InternalMessage(STATUS_FAULT| STATUS_FAULT_INVALID_DESTINATION, null);
        }

        uri = uri.replaceAll("^/", "");

        for (ServiceUtilities.ContentManager contentManager : _contentManagers) {
            if(!contentManager.accepts(uri)){
                Log.d("WebService", "Webservice did not accept uri" + uri + "\n on the basis of content.");
                return new InternalMessage(STATUS_FAULT|STATUS_FAULT_ACCESS_NOT_ALLOWED, null);
            }
        }

        try{
            FileInputStream stream = new FileInputStream(uri.replaceAll("^/", ""));
            InternalMessage returnMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_MESSAGE_IS_INPUTSTREAM, stream);
            return returnMessage;
        }catch(FileNotFoundException e){
            Log.d("WebService", "File not found: " + uri.replaceAll("^/", ""));
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_NOT_FOUND, null);
        }
    }

    /**
     * Quickbuilds an implementing Web Service.
     * @return The hub connected to the built Web Service
     */
    @WebMethod(exclude = true)
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
    @WebMethod(exclude = true)
    public Hub quickBuild(Class<? extends WebServiceConnector> connectorClass, Object... args) throws UnsupportedDataTypeException {
        SoapUnpackingHub hub = null;
        try {
            hub = new SoapUnpackingHub();
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
            _connection = connector;
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

    /**
     * Adds a {@link org.ntnunotif.wsnu.services.general.ServiceUtilities.ContentManager}.
     * @param manager
     */
    @WebMethod(exclude = true)
    public void addContentManager(ServiceUtilities.ContentManager manager){
        _contentManagers.add(manager);
    }

    /**
     * Removes a {@link org.ntnunotif.wsnu.services.general.ServiceUtilities.ContentManager}.
     * @param manager
     */
    @WebMethod(exclude = true)
    public void removeContentManger(ServiceUtilities.ContentManager manager){
        _contentManagers.remove(manager);
    }

    /**
     * Clears all {@link org.ntnunotif.wsnu.services.general.ServiceUtilities.ContentManager}'s.
     */
    @WebMethod(exclude = true)
    public void clearContentManagers(){
        _contentManagers.clear();
    }

    /**
     * Generate WSDL/XSD schemas.
     */
   @WebMethod(exclude = true)
   public void generateWSDLandXSDSchemas() throws Exception {

        if(endpointReference == null){
            throw new IllegalStateException("WebService must have endpointReference specified for creation of wsdl files");
        }

        String os = System.getProperty("os.name");
        String classPath = System.getProperty("java.class.path");


        File directory = new File(endpointReference);
        if(!directory.isDirectory()){
            directory.mkdir();
        }

        File file = new File(endpointReference+"/"+this.getClass().getSimpleName()+"Service.wsdl");

        if(file.isFile()){
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Log.d("WebService", "There already is a wsdl file generated for this Web Service, are you sure you want to make a new one? (y/n)");
            String in;
            while((in = reader.readLine()) != null){
                if(in.matches("^[Yy](.*)?")){
                    break;
                }else if(in.matches("^[Nn](.*)?")){
                    return;
                }else{
                    Log.d("WebService", "Invalid input, try again (y/n)");
                    continue;
                }
            }
        }

        if(os.equals("Windows")){
            //TODO:
        }
        if(os.equals("Linux")){
            String command = "wsgen -cp " + classPath + " -d "+ endpointReference+"/" +" -wsdl " + this.getClass().getCanonicalName();
            Log.d("WebService", "[Running command]: " + command);
            Process procces = Runtime.getRuntime().exec(command);
            procces.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(procces.getInputStream()));
            String out;

            while((out = reader.readLine()) != null){
                System.out.println(out);
            }
        }else if(os.equals("Windows")){
        }
        //TODO: Add support for more systems
    }
}
