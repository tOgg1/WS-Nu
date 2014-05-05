package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.internal.*;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;

import javax.activation.UnsupportedDataTypeException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * The parent of all web services implemented in this module. The main functionality of this class is storing an _endpointReference,
 * holding a reference to the hub (must be supplemented in any implementation class' constructor) as well as some utility methods.
 * @author Tormod Haugland
 * Created by tormod on 23.03.14.
 */
public abstract class WebService {

    /**
     * Contentmanagers for pure requests.
     */
    protected ArrayList<ServiceUtilities.ContentManager> _contentManagers;

    /**
     *
     */
    public ObjectFactory baseFactory = new ObjectFactory();

    /**
     * Reference to the connected hub
     */
    protected Hub _hub;

    /**
     * Default constructor.
     */
    protected WebService(){
        _contentManagers = new ArrayList<>();
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
     * The location of this webservices wsdl-file
     */
    protected String wsdlLocation;

    /**
     * Retrieves the endpointreference of the Web Service.
     * @return
     */
    public String getEndpointReference() {
        return endpointReference;
    }

    /**
     * Retrieves the endpointreference of the Web Service in the form of a {@link javax.xml.ws.wsaddressing.W3CEndpointReference} object.
     * @return
     */
    public W3CEndpointReference getEndpointReferenceAsW3C() {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(endpointReference);
        return builder.build();
    }

    /**
     * Sets the endpointreference of this hub. Note that this function fetches the Applicationserver's listening IP
     * (if several IP's are listed, the applicationserver chooses which to return. If you need a specific one, please call {@link }
     * {@link #forceEndpointReference(String)} ) and append the endpointreference to it.
     * Thus, if you want to give a web service the endpoint reference http://serverurl.domain/myWebService,
     * you only have to pass in "myWebService".
     *
     * @param endpointReference
     * @throws java.lang.IllegalArgumentException if the argument contains a backslash
     * @throws java.lang.IllegalStateException if hub is not set
     */
    public void setEndpointReference(String endpointReference) {
        if(endpointReference.contains("\\")){
            throw new IllegalArgumentException("EndpointReference can not contain the character \\(backslash)");
        }

        if(endpointReference.matches("^(https?://)(.*)?")){
            this.forceEndpointReference(endpointReference);
            return;
        }

        if(_hub == null){
            String errorMessage = "Hub is not set for this Web Service, please set the hub, " +
                    "(i.e by calling quickBuild) before using this method. You can also force " +
                    "the reference by calling forceEndpointReference";
            Log.e("WebService", errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        if (_connection == null) {
            Log.d("WebService", "Connection is not set for this Web Service. Endpoint reference is meaningless if " +
                    "there is no way this service can accept messages, so to use it, set up a connection.");
        }

        this.pureEndpointReference = endpointReference;
        this.endpointReference = _hub.getInetAdress() + "/" + endpointReference;

        if (_connection != null)
            _connection.endpointUpdated(this.endpointReference);
    }

    /**
     * Forces the endpoint reference to the endpointreference set.
     *
     * @param endpointReference
     * @throws java.lang.IllegalStateException if the {@link org.ntnunotif.wsnu.base.internal.ServiceConnection} is not set
     */
    public void forceEndpointReference(String endpointReference){
        this.endpointReference = endpointReference;
        this.pureEndpointReference = ServiceUtilities.filterEndpointReference(endpointReference);
        if(_connection == null) {
            Log.w("WebService", "Tried to force an endpoint reference for a null connector.");
            return;
        }
        _connection.endpointUpdated(endpointReference);
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
     * The default AcceptRequest method of a WebService. This handles requests by looking for matching files, and nothing more.
     * If specific requests, in particular with parameters, needs to be handled, this method should then be overrided.
     * @return
     */
    public InternalMessage acceptRequest(){
        RequestInformation requestInformation = _connection.getRequestInformation();

        String uri = requestInformation.getRequestURL();
        Log.d("WebService", "Request accepted: " + uri);
        Map<String, String[]> parameters = requestInformation.getParameters();

        if(parameters != null){
            if(parameters.size() != 0){
                for (String s : parameters.keySet()) {
                    Log.d("WebService", "Found parameter " + s);
                    /* This is a request for the wsdl files */
                    if(s.equals("wsdl")){
                        try{
                            if(wsdlLocation == null){
                                Log.e("WebService", "Wsdl-file does not exist, or is not set, please set it with setWsdlLocation() or run generateWsdlAndXsd()");
                                return new InternalMessage(STATUS_FAULT|STATUS_FAULT_NOT_FOUND, null);
                            }
                            FileInputStream stream = new FileInputStream(wsdlLocation);
                            InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_MESSAGE_IS_INPUTSTREAM, stream);
                            return message;
                        }catch(FileNotFoundException e){
                            Log.d("WebService", "Wsdl-file not found, please generate it with generateWsdlAndXsd()");
                            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_NOT_FOUND, null);
                        }
                    }
                }
            }
        }

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

    public InternalMessage sendRequest(String address, String request){
        return sendRequest(address + request);
    }

    public InternalMessage sendRequest(String requestUri){
        InternalMessage outMessage = new InternalMessage(STATUS_OK, null);
        RequestInformation info = new RequestInformation();
        info.setEndpointReference(requestUri);
        outMessage.setRequestInformation(info);
        return _hub.acceptLocalMessage(outMessage);
    }

    /**
     * Sends a subscriptionrequest with a termination time of one day (default hardcoded value, if anything else is wanted,
     * call {@link #sendSubscriptionRequest(String, String)}.
     * @param address
     * @return
     */
    public InternalMessage sendSubscriptionRequest(String address){
        return sendSubscriptionRequest(address, "P1D");
    }

    public InternalMessage sendSubscriptionRequest(String address, String terminationTime){
        Subscribe subscribe = new Subscribe();

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(getEndpointReference());

        W3CEndpointReference reference = builder.build();
        subscribe.setConsumerReference(reference);

        subscribe.setInitialTerminationTime(baseFactory.createSubscribeInitialTerminationTime(terminationTime));

        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, subscribe);
        message.getRequestInformation().setEndpointReference(address);
        return _hub.acceptLocalMessage(message);
    }

    public InternalMessage sendUnsubscribeRequest(String subscriptionEndpoint){
        Unsubscribe unsubscribe = new Unsubscribe();

        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, unsubscribe);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return _hub.acceptLocalMessage(message);
    }

    public InternalMessage sendRenewRequest(String subscriptionEndpoint){
        return sendRenewRequest(subscriptionEndpoint, "P1D");
    }

    public InternalMessage sendRenewRequest(String subscriptionEndpoint, String terminationTime){
        Renew renew = new Renew();
        renew.setTerminationTime(terminationTime);
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, renew);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return _hub.acceptLocalMessage(message);
    }

    public InternalMessage sendPauseRequest(String subscriptionEndpoint){
        PauseSubscription pauseSubscription = new PauseSubscription();
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, pauseSubscription);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return _hub.acceptLocalMessage(message);
    }

    public InternalMessage sendResumeRequest(String subscriptionEndpoint){
        ResumeSubscription resumeSubscription = new ResumeSubscription();
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, resumeSubscription);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return _hub.acceptLocalMessage(message);
    }

    public InternalMessage sendPublisherRegistrationRequest(String brokerEndpoint, long terminationTime, boolean demand){
        RegisterPublisher registerPublisher = new RegisterPublisher();
        registerPublisher.setPublisherReference(getEndpointReferenceAsW3C());
        registerPublisher.setDemand(demand);
        try {
            GregorianCalendar now = new GregorianCalendar();
            now.setTime(new Date(terminationTime));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            registerPublisher.setInitialTerminationTime(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("WebService", "Something went wrong while creating XMLGregoriancalendar");
        }
        InternalMessage outMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, registerPublisher);
        outMessage.getRequestInformation().setEndpointReference(brokerEndpoint);
        return _hub.acceptLocalMessage(outMessage);
    }


    public InternalMessage sendPublisherRegistrationRequest(String brokerEndpoint) {
        return sendPublisherRegistrationRequest(brokerEndpoint, System.currentTimeMillis()+86400, false);
    }

    public InternalMessage sendPublisherRegistrationRequest(String brokerEndpoint, String date, boolean demand) {
        try {
            long rawDate = ServiceUtilities.interpretTerminationTime(date);
            return sendPublisherRegistrationRequest(brokerEndpoint, rawDate, demand);
        } catch (UnacceptableTerminationTimeFault unacceptableTerminationTimeFault) {
                    Log.e("WebService", "Could not parse date");
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INTERNAL_ERROR, null);
        }
    }

    public InternalMessage sendGetCurrentMessage(String endpoint, TopicExpressionType topic){
        GetCurrentMessage getCurrentMessage = new GetCurrentMessage();
        getCurrentMessage.setTopic(topic);
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, getCurrentMessage);

        RequestInformation requestInformation = new RequestInformation();
        requestInformation.setEndpointReference(endpoint);
        message.setRequestInformation(requestInformation);

        return _hub.acceptLocalMessage(message);
    }

    public String fetchRemoteWsdl(String endpoint){
        String uri = endpoint + "?wsdl";
        InternalMessage returnMessage = sendRequest(uri);

        if((returnMessage.statusCode & STATUS_HAS_MESSAGE ) == 0){
            Log.e("WebService.fetchRemoteWsdl", "Wsdl not found");
        }

        if((returnMessage.statusCode & STATUS_FAULT) > 0){
            if((returnMessage.statusCode & STATUS_FAULT_INTERNAL_ERROR) > 0){
                Log.e("WebService.fetchRemoteWsdl", "Some error occured remotely: " + returnMessage.getRequestInformation().getHttpStatus());
            }else{
                Log.e("WebService.fetchRemoteWsdl", "Some internal error occured" + returnMessage.statusCode);
            }
        }

        if((returnMessage.statusCode & STATUS_OK) > 0){
            try{
                return (String)returnMessage.getMessage();
            }catch(ClassCastException e){
                try{
                    return new String((byte[])returnMessage.getMessage());
                }catch(ClassCastException f){
                    Log.e("WebService.fetchRemoteWsdl", "The returnMessage was not a String, or a byte[]. Please use either of these" +
                            "when returning data to this method.");
                    return null;
                }
            }
        }

        Log.e("WebService.fetchRemoteWsdl", "Incorrect flags was set before the the InternalMessage was returned to this method." +
                "Please set STATUS_OK | STATUS_HAS_MESSAGE | STATUS_MESSAGE_IS_INPUTSTREAM if everything went okey");
        return null;
    }

    /**
     * Quickbuilds an implementing Web Service.
     * @return The hub connected to the built Web Service
     */
    public SoapForwardingHub quickBuild(String endpointReference) {
        try {
            // Ensure the application server is stopped.
            ApplicationServer.getInstance().stop();

            SoapForwardingHub hub = new SoapForwardingHub();
            _hub = hub;

            this.setEndpointReference(endpointReference);

            // Start the application server with this hub
            ApplicationServer.getInstance().start(hub);

            UnpackingConnector connector = new UnpackingConnector(this);
            hub.registerService(connector);
            _connection = connector;

            return hub;
        } catch (Exception e) {
            throw new RuntimeException("Unable to quickbuild: " + e.getMessage());
        }
    }

    public void quickBuild(String endpointReference, Hub hub){
        _hub = hub;
        this.setEndpointReference(endpointReference);

        UnpackingConnector connector = new UnpackingConnector(this);
        hub.registerService(connector);
        _connection = connector;
    }

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
        SoapForwardingHub hub = null;
        try {
            hub = new SoapForwardingHub();
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
    public void addContentManager(ServiceUtilities.ContentManager manager){
        _contentManagers.add(manager);
    }

    /**
     * Removes a {@link org.ntnunotif.wsnu.services.general.ServiceUtilities.ContentManager}.
     * @param manager
     */
    public void removeContentManger(ServiceUtilities.ContentManager manager){
        _contentManagers.remove(manager);
    }

    /**
     * Clears all {@link org.ntnunotif.wsnu.services.general.ServiceUtilities.ContentManager}'s.
     */
    public void clearContentManagers(){
        _contentManagers.clear();
    }

    /**
     * Generate WSDL/XSD schemas.
     */
    public void generateWSDLandXSDSchemas() throws Exception {

        //TODO: Add support for windows-commandline

        if(endpointReference == null){
            throw new IllegalStateException("WebService must have endpointReference specified for creation of wsdl files");
        }

        String os = System.getProperty("os.name");
        String classPath = System.getProperty("java.class.path");

        File directory = new File(pureEndpointReference);
        if(!directory.isDirectory()){
            directory.mkdir();
        }

        File file = new File(pureEndpointReference+"/"+this.getClass().getSimpleName()+"Service.wsdl");
        wsdlLocation = pureEndpointReference+"/"+this.getClass().getSimpleName()+"Service.wsdl";

        // add to path
        String command = "wsgen -cp \"" + classPath + "\" -d "+ pureEndpointReference+"/" +" -wsdl " + this.getClass().getCanonicalName();
        Log.d("WebService", "[Running command]: " + command);

        Process procces = Runtime.getRuntime().exec(command);
        procces.waitFor();

        Log.d("WebService", "Normal output:");
        BufferedReader normalOutputReader = new BufferedReader(new InputStreamReader(procces.getInputStream()));
        String out;

        while((out = normalOutputReader.readLine()) != null){
            System.out.println(out);
        }

        Log.d("WebService", "Error output:");
        BufferedReader errorOutputReader = new BufferedReader(new InputStreamReader(procces.getErrorStream()));

        while((out = errorOutputReader.readLine()) != null){
            System.out.println(out);
        }

        normalOutputReader.close();
        errorOutputReader.close();

        Log.d("WebService", "Generation completed");

        FileInputStream newStream = new FileInputStream(pureEndpointReference+"/"+this.getClass().getSimpleName()+"Service.wsdl");
        StringBuilder sb = new StringBuilder();

        int c;

        while((c = newStream.read()) != -1){
            sb.append((char)c);
        }

        String wsdl = sb.toString();
        wsdl = wsdl.replaceAll("soap:address location=\"REPLACE_WITH_ACTUAL_URL\"/>", "soap:address location=\""+endpointReference+"\"/>");


        byte[] bytes = wsdl.getBytes();

        FileOutputStream outStream = new FileOutputStream(pureEndpointReference+"/"+this.getClass().getSimpleName()+"Service.wsdl");
        outStream.write(bytes);
        outStream.close();

    }

    public String getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * Sets the location of the wsdl files associated with this Web Service.
     * @param wsdlLocation
     * @return True if the file was found, false if not.
     */
    public boolean setWsdlLocation(String wsdlLocation) {
        File file = new File(wsdlLocation);

        if(!file.isFile())
            return false;

        this.wsdlLocation = wsdlLocation;
        return true;
    }

    public ServiceConnection getConnection() {
        return _connection;
    }

    public void setConnection(ServiceConnection connection) {
        this._connection = connection;
    }
}
