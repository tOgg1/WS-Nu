//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.internal.*;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;

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
 * Base class for all Web Services implemented by WS-Nu. Can also serve as a base-class for any other base
 */
public abstract class WebService {

    /**
     * ContentManagers to filter file-requests. See {@link org.ntnunotif.wsnu.services.general.ServiceUtilities.ContentManager}
     * for more information.
     */
    protected ArrayList<ServiceUtilities.ContentManager> contentManagers;

    /**
     * BaseFactory to create relevant objects.
     */
    //TODO: This should be removed, and generalized.
    public ObjectFactory baseFactory = new ObjectFactory();

    /**
     * Reference to the connected hub.
     */
    protected Hub hub;

    /**
     * Default constructor.
     */
    protected WebService(){
        contentManagers = new ArrayList<>();
    }

    /**
     * Constructor taking a hub as a parameter.
     * @param hub
     */
    protected WebService(Hub hub) {
        this.hub = hub;
    }

    /**
     * Reference to the connection of this Web Service. This variable carries the ServiceConnection
     */
    @Connection
    protected ServiceConnection connection;

    /**
     * The full endpointreference of this webservice. This would typically be
     */
    @EndpointReference
    protected String endpointReference;

    /**
     * The short version of the endpointReference. If the endpoint reference is 133.371.337.133/endpoint,
     * this value would be endpoint.
     */
    protected String pureEndpointReference;

    /**
     * The location of the Web Service's wsdl-file. This should be a path relative to execution-path of the program.
     */
    protected String wsdlLocation;

    /**
     * Retrieves the endpoint reference of the Web Service.
     * @return
     */
    public String getEndpointReference() {
        return endpointReference;
    }

    /**
     * @return the endpoint reference of the Web Service in the form of a {@link javax.xml.ws.wsaddressing.W3CEndpointReference} object.
     */
    public W3CEndpointReference getEndpointReferenceAsW3C() {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(endpointReference);
        return builder.build();
    }

    /**
     * Sets the endpointreference of this hub. Note that this function fetches the {@link org.ntnunotif.wsnu.base.internal.Hub} listening IP
     * If several IP's are listed, the Hub chooses which to return. The method called for this operation is {@link org.ntnunotif.wsnu.base.internal.Hub#getInetAdress()}
     * If you need a specific address, please call {@link #forceEndpointReference(String)} ).
     *</p>
     * Thus with this method, if you want to give a web service the endpoint reference http://serverurl.domain/myWebService,
     * you only have to pass in "myWebService".
     *</p>
     * If the method receives a full url. It will redirect the request to {@link #forceEndpointReference(String)}.
     *</p>
     *
     * @param endpointReference The endpoint reference request for this Web Service.
     * @throws {@link java.lang.IllegalArgumentException} if the argument contains a backslash.
     * @throws {@link java.lang.IllegalStateException} if hub is not set.
     */
    public void setEndpointReference(String endpointReference) {
        if(endpointReference.contains("\\")){
            throw new IllegalArgumentException("EndpointReference can not contain the character \\(backslash)");
        }

        if(endpointReference.matches("^(https?://)(.*)?")){
            this.forceEndpointReference(endpointReference);
            return;
        }

        if(hub == null){
            String errorMessage = "Hub is not set for this Web Service, please set the hub, " +
                    "(i.e by calling quickBuild) before using this method. You can also force " +
                    "the reference by calling forceEndpointReference";
            Log.e("WebService", errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        if (connection == null) {
            Log.d("WebService", "Connection is not set for this Web Service. Endpoint reference is meaningless if " +
                    "there is no way this service can accept messages, so to use it, set up a connection.");
        }

        this.pureEndpointReference = endpointReference;
        this.endpointReference = hub.getInetAdress() + "/" + endpointReference;

        if (connection != null)
            connection.endpointUpdated(this.endpointReference);
    }

    /**
     * Forces the endpoint reference. The difference between this method and {@link #setEndpointReference(String)},
     * is that while the setEndpoitnReference appends the passed in argument to the connected {@link org.ntnunotif.wsnu.base.internal.Hub}'s
     * IP, this method forces the endpoint reference to be whatever passed in. E.g. if the passed in argument is "http://lol.com", the actual
     * endpoint reference variable will be set as "http://lol.com".
     *
     * A warning message will be shown if the {@link #connection} variable is null on calling this message.
     *
     * @param endpointReference A valid url for this Web Service. Passing in a bad address can cause undefined behaviour.
     */
    public void forceEndpointReference(String endpointReference){
        this.endpointReference = endpointReference;
        this.pureEndpointReference = ServiceUtilities.filterEndpointReference(endpointReference);

        if(connection == null) {
            Log.w("WebService", "Tried to force an endpoint reference for a null connector.");
            return;
        }
        connection.endpointUpdated(endpointReference);
    }

    /**
     * Getter for the hub. Does nothing special.
     * @return The connected {@link org.ntnunotif.wsnu.base.internal.Hub}
     */
    public Hub getHub() {
        return hub;
    }

    /**
     * Setter for the hub. Does nothing special.
     * @param _hub A {@link org.ntnunotif.wsnu.base.internal.Hub}.
     */
    public void setHub(Hub _hub) {
        this.hub = _hub;
    }

    /**
     * The default AcceptRequest method of a WebService. This method accepts what we call pure-requests. These are
     * http-requests containing no content.
     * </p>
     * This implementation will look for a ?wsdl parameter. If not found, it will try and retrieve a file at the
     * location specified.
     * </p>
     * Example: Say our web service is located at
     * <code>http://server.com/endpoint</code>,
     * and the following request is received:
     * <code>http://server.com/endpoint/some/folder/file.txt</code>
     * </p>
     *
     * If the file some/folder/file.txt exists, path being relative to the execution folder of the Web Service,
     * the file is returned as a string.
     *
     * See {@link org.ntnunotif.wsnu.base.internal.WebServiceConnector#acceptRequest(org.ntnunotif.wsnu.base.util.InternalMessage)}
     * for more information regarding how requests are handled.
     *
     * @return An InternalMessage containing possible content.
     */
    public InternalMessage acceptRequest(){
        RequestInformation requestInformation = connection.getRequestInformation();

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

        for (ServiceUtilities.ContentManager contentManager : contentManagers) {
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
     * Helper function to send a request to an address. The entirity of this function is:
     *
     * <code>
     *      return sendRequest(address + request);
     * </code>
     *
     * The method called is the overloaded {@link #sendRequest(String)}.
     *
     * @param address The destination address. E.g. http://example.com/webService
     * @param request The request. This can be any valid url-request. I.e. ?recipe=fa23fabfd&AmIHungry=Yes&WouldIRatherEatThanWriteJavaDoc=Yes
     * @return The content received from the server, if any. The InternalMessage will also contain a {@link org.ntnunotif.wsnu.base.util.RequestInformation} object
     */
    public InternalMessage sendRequest(String address, String request){
        return sendRequest(address + request);
    }

    /**
     * Helper function to send a request to an address.
     * @param requestUri The actual request-url. E.g. http://example.com/webService?EvenMoreHungryNow=Yes
     * @return The content received from the server, if any. The InternalMessage will also contain a {@link org.ntnunotif.wsnu.base.util.RequestInformation} object
     */
    public InternalMessage sendRequest(String requestUri){
        InternalMessage outMessage = new InternalMessage(STATUS_OK, null);
        RequestInformation info = new RequestInformation();
        info.setEndpointReference(requestUri);
        outMessage.setRequestInformation(info);
        return hub.acceptLocalMessage(outMessage);
    }

    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
    public InternalMessage sendSubscriptionRequest(String address){
        return sendSubscriptionRequest(address, "P1D");
    }

    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
    public InternalMessage sendSubscriptionRequest(String address, String terminationTime){
        Subscribe subscribe = new Subscribe();

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(getEndpointReference());

        W3CEndpointReference reference = builder.build();
        subscribe.setConsumerReference(reference);

        subscribe.setInitialTerminationTime(baseFactory.createSubscribeInitialTerminationTime(terminationTime));

        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, subscribe);
        message.getRequestInformation().setEndpointReference(address);
        return hub.acceptLocalMessage(message);
    }

    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
    public InternalMessage sendUnsubscribeRequest(String subscriptionEndpoint){
        Unsubscribe unsubscribe = new Unsubscribe();

        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, unsubscribe);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return hub.acceptLocalMessage(message);
    }

    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
    public InternalMessage sendRenewRequest(String subscriptionEndpoint){
        return sendRenewRequest(subscriptionEndpoint, "P1D");
    }

    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
    public InternalMessage sendRenewRequest(String subscriptionEndpoint, String terminationTime){
        Renew renew = new Renew();
        renew.setTerminationTime(terminationTime);
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, renew);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return hub.acceptLocalMessage(message);
    }

    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
    public InternalMessage sendPauseRequest(String subscriptionEndpoint){
        PauseSubscription pauseSubscription = new PauseSubscription();
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, pauseSubscription);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return hub.acceptLocalMessage(message);
    }

    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
    public InternalMessage sendResumeRequest(String subscriptionEndpoint){
        ResumeSubscription resumeSubscription = new ResumeSubscription();
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, resumeSubscription);
        message.getRequestInformation().setEndpointReference(subscriptionEndpoint);
        return hub.acceptLocalMessage(message);
    }

    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
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
        return hub.acceptLocalMessage(outMessage);
    }


    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
    public InternalMessage sendPublisherRegistrationRequest(String brokerEndpoint) {
        return sendPublisherRegistrationRequest(brokerEndpoint, System.currentTimeMillis()+86400, false);
    }

    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
    public InternalMessage sendPublisherRegistrationRequest(String brokerEndpoint, String date, boolean demand) {
        try {
            long rawDate = ServiceUtilities.interpretTerminationTime(date);
            return sendPublisherRegistrationRequest(brokerEndpoint, rawDate, demand);
        } catch (UnacceptableTerminationTimeFault unacceptableTerminationTimeFault) {
                    Log.e("WebService", "Could not parse date");
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INTERNAL_ERROR, null);
        }
    }

    /**
     * JavaDoc missing until this method is moved, see the issue <href>https://github.com/tOgg1/WS-Nu/issues/10</href>
     */
    public InternalMessage sendGetCurrentMessage(String endpoint, TopicExpressionType topic){
        GetCurrentMessage getCurrentMessage = new GetCurrentMessage();
        getCurrentMessage.setTopic(topic);
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, getCurrentMessage);

        RequestInformation requestInformation = new RequestInformation();
        requestInformation.setEndpointReference(endpoint);
        message.setRequestInformation(requestInformation);

        return hub.acceptLocalMessage(message);
    }

    /**
     * Fetches a remote wsdl-file. The method expects a valid endpoint reference, and then attaches a "?wsdl"
     * parameter to it.
     * @return The wsdl-file in the form of a string if the wsdl file is found. If not, null is returned.
     */
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
     * Quick builds this Web Service along with the rest of the system. The order this is done in, is:
     *
     * <ol>
     *     <li>Create a {@link org.ntnunotif.wsnu.base.internal.SoapForwardingHub}</li>
     *     <li>Set endpoint reference</li>
     *     <li>Start the {@link org.ntnunotif.wsnu.base.net.ApplicationServer}</li>
     *     <li>Create an {@link org.ntnunotif.wsnu.base.internal.UnpackingConnector} with this Web Service attached</li>
     *     <li>Register the connector at the Hub</li>
     * </ol>
     *
     * If another Connector is needed, either call {@link #quickBuild(Class, Object...)} or manually do all of the above.
     * @see {@link #quickBuild(Class, Object...)}
     * @see {@link #quickBuild(String, org.ntnunotif.wsnu.base.internal.Hub)}
     * @return The Hub connected to the built Web Service
     */
    public SoapForwardingHub quickBuild(String endpointReference) {
        try {
            // Ensure the application server is stopped.
            ApplicationServer.getInstance().stop();

            SoapForwardingHub hub = new SoapForwardingHub();
            this.hub = hub;

            this.setEndpointReference(endpointReference);

            // Start the application server with this hub
            ApplicationServer.getInstance().start(hub);

            UnpackingConnector connector = new UnpackingConnector(this);
            hub.registerService(connector);
            connection = connector;

            return hub;
        } catch (Exception e) {
            throw new RuntimeException("Unable to quickbuild: " + e.getMessage());
        }
    }

    /**
     * Quick builds this web service. As it takes in a Hub, this method assumes that most of the system
     * is already built. This method does the following:
     *
     * <ol>
     *     <li>Sets the endpoint reference</li>
     *     <li>Create an {@link org.ntnunotif.wsnu.base.internal.UnpackingConnector} with this Web Service attached</li>
     *     <li>Register the connector at the Hub</li>
     * </ol>
     *
     * If another Connector is needed, either call {@link #quickBuild(Class, Object...)} or manually do all of the above.
     * @see {@link #quickBuild(String)}
     * @see {@link #quickBuild(Class, Object...)}
     * @return The Hub connected to the built Web Service
     */
    public void quickBuild(String endpointReference, Hub hub){
        this.hub = hub;
        this.setEndpointReference(endpointReference);

        UnpackingConnector connector = new UnpackingConnector(this);
        hub.registerService(connector);
        connection = connector;
    }

    /**
     * Quick Builds this Web Service.
     *
     * Note that it is assumed that the constructor takes this Web Service as its first argument.
     *
     * This method takes as arguments the class of the connector and the arguments to passed
     * to it's constructor. This function catches {@link java.lang.reflect.InvocationTargetException} and any other {@link java.lang.Exception}.
     * By doing this it it stops the hub from constructing and throws an IllegalArgumentException.
     *
     * @param connectorClass The class of the wanted {@link org.ntnunotif.wsnu.base.internal.WebServiceConnector}
     * @param args All extra arguments
     * @return The Hub connected to the built Web Service
     */
    public Hub quickBuild(Class<? extends WebServiceConnector> connectorClass, Object... args) {
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
            connection = connector;
            this.hub = hub;
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
     * Adds a content manager.
     * @param manager A {@link org.ntnunotif.wsnu.services.general.ServiceUtilities.ContentManager}.
     */
    public void addContentManager(ServiceUtilities.ContentManager manager){
        contentManagers.add(manager);
    }

    /**
     * Removes a content manager.
     * @param manager A {@link org.ntnunotif.wsnu.services.general.ServiceUtilities.ContentManager}.
     */
    public void removeContentManger(ServiceUtilities.ContentManager manager){
        contentManagers.remove(manager);
    }

    /**
     * Clears all {@link org.ntnunotif.wsnu.services.general.ServiceUtilities.ContentManager}'s.
     */
    public void clearContentManagers(){
        contentManagers.clear();
    }

    /**
     * Generate WSDL/XSD schemas for this Web Service. If successful, it puts these files in a folder endpointReferenceOfYourService/
     * The way it generates the files is with the command ""wsgen -cp "yourClassPath" -d "endpointReferenceFolder" -wsdl "classOfYourWebService""
     * This requires that the system it runs on has a JDK installed. As wsgen is not included in any JRE.
     * </p>
     *
     * Note that the use of this method should be limited. It is not modifiable, and it is as easy running this command in
     * a terminal yourself.
     *
     * @throws java.lang.Exception Throws any exception it encounters. If anything goes wrong, this method will do nothing to try harder.
     *
     */
    public void generateWSDLandXSDSchemas() throws Exception {

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

    /**
     * Get the location of the wsdl-file.
     * @return The relative path of the wsdl-file.
     */
    public String getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * Sets the location of the wsdl files associated with this Web Service.
     * @param wsdlLocation The relative path of the wsdl location.
     * @return True if the file was found, false if not.
     */
    public boolean setWsdlLocation(String wsdlLocation) {
        File file = new File(wsdlLocation);

        if(!file.isFile()) {
            return false;
        }

        this.wsdlLocation = wsdlLocation;
        return true;
    }

    /**
     * @return The {@link org.ntnunotif.wsnu.base.internal.ServiceConnection} connected to this Web Service.
     */
    public ServiceConnection getConnection() {
        return connection;
    }

    /**
     * Set the {@link org.ntnunotif.wsnu.base.internal.ServiceConnection} connected to this Web Service.
     *
     * If this method is called without explicit invocations of {@link org.ntnunotif.wsnu.base.internal.Hub#registerService(org.ntnunotif.wsnu.base.internal.ServiceConnection)}
     * (i.e. if the connection is not registered with the Hub), and possibly deleting old connections, occurance of undefined behaviour is possible.
     * @param connection
     */
    public void setConnection(ServiceConnection connection) {
        this.connection = connection;
    }
}
