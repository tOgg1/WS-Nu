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

package org.ntnunotif.wsnu.base.net;

import com.google.common.io.ByteStreams;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Enumeration;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Implementation of jetty's application server. Implemented as singleton to avoid multiple instantiations which MIGHT cause port-bind exceptions.
 * @Author: Tormod Haugland
 * @Date: 06/03/2014
 */
public class ApplicationServer{

    /**
     * Singleton instance.
     */
    private static ApplicationServer _singleton = null;

    /**
     * Jetty-http server instance.
     */
    private Server _server;

    /**
     * The server's connectors.
     */
    private ArrayList<Connector> _connectors = new ArrayList<>();

    /**
     * Jetty-http client.
     */
    private HttpClient _client;

    /**
     * Thread for the server to run on.
     */
    private Thread _serverThread;

    /**
     * A bus object as parent. Needed to reroute requests to bus.
     */
    private Hub _parentHub;

    /**
     * Variable to check if this server is running. Primarily used to avoid double @start calls.
     */
    private static boolean _isRunning = false;

    /**
     * Variable that signifies whether or getInstance() ever has been invoked.
     */
    private static boolean _hasBeenInvoked = false;

    /**
     * Configuration file for this server.
     */
    private static String _configFile = "defaultconfig.xml";

    /**
     * Public variable to toggle resource-loading from file
     */
    public static boolean useConfigFile = true;

    /**
     * Reference to our handler.
     */
    private static AbstractHandler _handler;

    /**
     * As this class is a singleton no external instantiation is allowed.
     */
    private ApplicationServer(){
        init();
    }

    /**
     * Inits the system by creating a new server and handler
     */
    private void init() {
        try {
            if (useConfigFile) {
                Resource resource = Resource.newSystemResource(_configFile);

                XmlConfiguration config = new XmlConfiguration(resource.getInputStream());
                _server = (Server) config.configure();

                _handler = new HttpHandler();
                _server.setHandler(_handler);
            } else {
                _server = new Server();
                _handler = new HttpHandler();
                _server.setHandler(_handler);
            }
        } catch(Exception e){
            throw new RuntimeException("Server unable to start: " + e.getMessage());
        }
    }

    /**
     * Function to return the singleton instance. Will create a new instance if no instance has yet been instantiated.
     * If any custom settings are to be set for this instance, they MUST be called before the first invocation of this method.
     * @return Returns the running singleton instance,
     */
    public static ApplicationServer getInstance() throws Exception{
        if(!_hasBeenInvoked){
            _singleton = new ApplicationServer();
            _hasBeenInvoked = true;
            return _singleton;
        }else{
            return _singleton;
        }
    }

    /**
     * Stop the http-server.
     * @throws java.lang.Exception Throws an exception if the server is unable to stop.
     */
    public void stop(){
        try {
            if(_server != null) {
                _server.stop();
            }

            System.out.println("Hello1");
            if(_serverThread != null) {
                _serverThread.join();
            }

            System.out.println("Hello2");
            if(_parentHub != null) {
                _parentHub = null;
            }

            _isRunning = false;
            _hasBeenInvoked = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the http-server.
     * @throws java.lang.Exception Throws an exception if the server is unable to stop.
     */
    public void start(Hub hub) throws Exception{
        if(_isRunning) {
            return;
        }

        _isRunning = true;
        _parentHub = hub;
        _client = new HttpClient();
        _client.setFollowRedirects(false);
        _client.start();

        for (Connector connector : _connectors) {
            _server.addConnector(connector);
        }

        /* Start server */
        try {
            _serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        _server.start();
                        _server.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            _serverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the server without a hub-conncetion, only allowing for messages to be sent out
     */
    public void startNoHub() throws Exception {
        if(_isRunning){
            return;
        }

        _isRunning = true;
        _client = new HttpClient();
        _client.setFollowRedirects(false);
        _client.start();

        for (Connector connector : _connectors) {
            _server.addConnector(connector);
        }

       /* Start server */
        try {
            _serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        _server.start();
                        _server.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            _serverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void restart() throws Exception{
        stop();
        init();
        start(_parentHub);
    }

    public static void setServerConfiguration(String pathToConfigFile) throws Exception{
        File f = new File(pathToConfigFile);

        if(!f.isFile()) {
            throw new IllegalArgumentException("Path pointed is not a file");
        }

        _configFile = pathToConfigFile;
    }

    public void addStandardConnector(String address, int port){
        ServerConnector connector = new ServerConnector(_server);
        connector.setHost(address);
        if(port == 80){
            Log.w("ApplicationServer", "You have requested to use port 80. This will not work unless you are running as root." +
                    "Are you running as root? You shouldn't. Reroute port 80 to 8080 instead.");
        }
        connector.setPort(port);
        _connectors.add(connector);
        _server.addConnector(connector);
    }

    public void addConnector(Connector connector){
        _connectors.add(connector);
        this._server.addConnector(connector);
    }

    /**
     * Sets the handler of this server. Calling this function will cause the server to restart
     * @param handler
     */
    public static void setHandler(AbstractHandler handler){
        _handler = handler;
        if(_isRunning){
            try {
                _singleton.restart();
            } catch (Exception e) {
                Log.e("ApplcationServer", "ApplicationServer crashed on restart");
                throw new RuntimeException("Server unable to restart");
            }
        }
    }

    /**
     * Gets this server object.
     * @return
     */
    public final Server getServer(){
        return _server;
    }

    /**
     * Takes a message as an inputStream and sends it to a recipient over HTML. This function expects a response,
     * and sends this response back up the system.
     * @param message
     * @return An array with <code>{int status, InputStream contentRecieved}</code>
     */
    public InternalMessage sendMessage(InternalMessage message){

        RequestInformation requestInformation = message.getRequestInformation();
        String endpoint = requestInformation.getEndpointReference();

        /* If we have nowhere to send the message */
        if(endpoint == null){
            Log.e("ApplicationServer", "Endpoint reference not set");
            return new InternalMessage(STATUS_FAULT, null);
        }

        /* Create the actual http-request*/
        org.eclipse.jetty.client.api.Request request = _client.newRequest(requestInformation.getEndpointReference());

        /* Try to send the message */
        try{
            /* Raw request */
            if((message.statusCode & STATUS_HAS_MESSAGE) == 0){
                request.method(HttpMethod.GET);
                Log.d("ApplicationServer", "Sending message without content to " + requestInformation.getEndpointReference());
                ContentResponse response = request.send();
                return new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, response.getContentAsString());
            }else{
                request.method(HttpMethod.POST);
                if((message.statusCode & STATUS_MESSAGE_IS_INPUTSTREAM) == 0){
                    Log.e("ApplicationServer.sendMessage", "The message contained something else than an inputStream." +
                          "Please convert your message to an InputStream before calling this methbod.");
                    return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
                }else{
                    if(message.getMessage() == null){
                        Log.e("ApplicationServer", "No content was found to send");
                        return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
                    }
                    Log.d("ApplicationServer", "Sending message with content to " + requestInformation.getEndpointReference());
                    request.content(new InputStreamContentProvider((InputStream) message.getMessage()), "application/soap+xml;charset/utf-8");
                    ContentResponse response = request.send();
                    if(response.getStatus() != HttpStatus.OK_200){
                        return new InternalMessage(STATUS_FAULT|STATUS_HAS_MESSAGE, response.getContentAsString());
                    } else {
                        return new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, response.getContentAsString());
                    }
                }
            }
        }catch(ClassCastException e){
            Log.e("ApplicationServer.sendMessage", "The message contained something else than an inputStream." +
                    "Please convert your message to an InputStream before calling this method.");
            return new InternalMessage(STATUS_FAULT|STATUS_FAULT_INVALID_PAYLOAD, null);
        }catch(Exception e) {
            if(e instanceof ConnectException){
                Log.e("ApplicationServer.sendMessage", "Unable to establish connection: " + e.getMessage());
            }
            return new InternalMessage(STATUS_FAULT_INTERNAL_ERROR, null);
        }
    }

    /**
     * WS-Nu's default http-handler.
     */
    private class HttpHandler extends AbstractHandler {

        /**
         * Empty constructor
         */
        private HttpHandler(){
        }

        /**
         * Handles a HttpRequest. Derived function from {@link org.eclipse.jetty.server.handler.AbstractHandler}
         *
         * @param s
         * @param request
         * @param httpServletRequest
         * @param httpServletResponse
         * @throws IOException
         * @throws ServletException
         */
        @Override
        public void handle(String s, org.eclipse.jetty.server.Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {

            if(_parentHub == null){
                httpServletResponse.setStatus(HttpStatus.NOT_FOUND_404);
                request.setHandled(true);
            }

            /* Handle headers */
            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();

            while(headerNames.hasMoreElements()){
                String headerName = headerNames.nextElement();
                Enumeration<String> headers = httpServletRequest.getHeaders(headerName);

                // TODO: Here we need to handle all headers that is necessary.
                // Temporary debugging
                while(headers.hasMoreElements()){
                    Log.d("ApplicationServer", headerName + "=" + headers.nextElement());
                }
            }

            Log.d("ApplicationServer", "Accepted message");

            /* Get content, if there is any */
            InternalMessage outMessage;
            if(httpServletRequest.getContentLength() > 0) {
                InputStream input = httpServletRequest.getInputStream();
                outMessage = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE, input);
            }else{
                outMessage = new InternalMessage(STATUS_OK, null);
            }

            /* Send the message to the hub */
            outMessage.getRequestInformation().setEndpointReference(request.getRemoteHost());
            outMessage.getRequestInformation().setRequestURL(request.getRequestURI());
            outMessage.getRequestInformation().setParameters(request.getParameterMap());
            Log.d("ApplicationServer", "Forwarding message to hub");
            InternalMessage returnMessage = ApplicationServer.this._parentHub.acceptNetMessage(outMessage, httpServletResponse.getOutputStream());

            /* Fatal error, is your hub designed correctly? */
            if(returnMessage == null){
                httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                request.setHandled(true);
                return;
            }

            /* Handle possible errors */
            if((returnMessage.statusCode & STATUS_FAULT) > 0){

                /* Have we got an error message to return? */
                if((returnMessage.statusCode & STATUS_HAS_MESSAGE) > 0){
                    httpServletResponse.setContentType("application/soap+xml;charset=utf-8");

                    InputStream inputStream = (InputStream)returnMessage.getMessage();
                    OutputStream outputStream = httpServletResponse.getOutputStream();

                    /* google.commons helper function*/
                    ByteStreams.copy(inputStream, outputStream);

                    httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    outputStream.flush();
                    request.setHandled(true);
                    return;
                }

                if((returnMessage.statusCode & STATUS_FAULT_INVALID_DESTINATION) > 0){
                    httpServletResponse.setStatus(HttpStatus.NOT_FOUND_404);
                    request.setHandled(true);
                    return;
                }else if((returnMessage.statusCode & STATUS_FAULT_INTERNAL_ERROR) > 0){
                    httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    request.setHandled(true);
                    return;
                }else if((returnMessage.statusCode & STATUS_FAULT_INVALID_PAYLOAD) > 0){
                    httpServletResponse.setStatus(HttpStatus.BAD_REQUEST_400);
                    request.setHandled(true);
                    return;
                }else if((returnMessage.statusCode & STATUS_FAULT_ACCESS_NOT_ALLOWED) > 0){
                    httpServletResponse.setStatus(HttpStatus.FORBIDDEN_403);
                    request.setHandled(true);
                }
                httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                request.setHandled(true);
                return;
            }else if(((STATUS_OK & returnMessage.statusCode) > 0) &&
                      (STATUS_HAS_MESSAGE & returnMessage.statusCode) > 0){

                /* Liar liar pants on fire */
                if(returnMessage.getMessage() == null){
                    Log.e("ApplicationServer", "The HAS_RETURNING_MESSAGE flag was checked, but there was no returning message");
                    httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    request.setHandled(true);
                    return;
                }

                httpServletResponse.setContentType("application/soap+xml;charset=utf-8");

                InputStream inputStream = (InputStream)returnMessage.getMessage();
                OutputStream outputStream = httpServletResponse.getOutputStream();

                /* google.commons helper function*/
                ByteStreams.copy(inputStream, outputStream);

                httpServletResponse.setStatus(HttpStatus.OK_200);
                outputStream.flush();
                request.setHandled(true);
            /* Everything is fine, and nothing is expected */
            }else if((STATUS_OK & returnMessage.statusCode) > 0){
                httpServletResponse.setStatus(HttpStatus.OK_200);
                request.setHandled(true);
            }else{
                Log.w("ApplicationServer.handleMessage", "The message returned to the ApplcationServer was not flagged with either STATUS_OK or" +
                      "STATUS_FAULT. Please set either of these flags at all points");
                httpServletResponse.setStatus(HttpStatus.OK_200);
                request.setHandled(true);
            }
        }
    }

    //TODO: Fix a better system
    public static String getURI(){
        if(_singleton._server.getURI() == null){
            return "http://0.0.0.0:8080";
        }
        return "http://" + _singleton._server.getURI().getHost()+ ":" + (_singleton._server.getURI().getPort() > -1 ? _singleton._server.getURI().getPort() : 8080);
    }
}
