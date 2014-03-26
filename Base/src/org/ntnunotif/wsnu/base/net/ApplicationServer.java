package org.ntnunotif.wsnu.base.net;

import com.google.common.io.ByteStreams;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;
import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_FAULT;

/**
 * Implementation of jetty's application server. Implemented as singleton to avoid multiple instantiations which MIGHT cause port-bind exceptions.
 * @Author: Tormod Haugland
 * @Date: 06/03/2014
 */
//TODO: Add modifiability for the server
public class ApplicationServer{

    /**
     * Singleton instance.
     */
    private static ApplicationServer _singleton = null;

    /**
     * Jetty-http server instance.
     */
    private static Server _server;

    /**
     * The server's connectors.
     */
    private ArrayList<Connector> _connectors;

    /**
     * Jetty-http client.
     */
    private static HttpClient _client;

    /**
     * Thread for the server to run on.
     */
    private static Thread _serverThread;

    /**
     * A bus object as parent. Needed to reroute requests to bus.
     */
    private static Hub _parentHub;

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
     * As this class is a singleton no external instantiation is allowed.
     */
    private ApplicationServer() throws Exception
    {
        Resource resource = Resource.newSystemResource(_configFile);

        XmlConfiguration config = new XmlConfiguration(resource.getInputStream());
        _server = (Server)config.configure();
        _server.setHandler(new HttpHandler());
    }

    public static void setServerConfiguration(String pathToConfigFile) throws Exception{
        File f = new File(pathToConfigFile);

        if(!f.isFile())
            throw new IllegalArgumentException("Path pointed is not a file");
        _configFile = pathToConfigFile;
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
     * Start the http-server.
     * @throws java.lang.Exception Throws an exception if the server is unable to stop.
     */
    public void start(ForwardingHub forwardingHub) throws Exception{
        if(_isRunning){
            return;
        }

        _isRunning = true;
        _parentHub = forwardingHub;
        _client = new HttpClient();
        _client.setFollowRedirects(false);
        _client.start();

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
     * Stop the http-server.
     * @throws java.lang.Exception Throws an exception if the server is unable to stop.
     */
    public void stop(){
        try {
            _server.stop();
            _serverThread.join();
            _isRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes a message as an inputStream and sends it to a recipient over HTML. This function expects a response,
     * and sends this response back up the system.
     * @param message
     * @return An array with <code>{int status, InputStream contentRecieved}</code>
     */
    public InternalMessage sendMessage(InternalMessage message){
        //TODO: Distinguish between different faults?
        //TODO: Handle outputstreams in message.getMessage() here? It is already handled in hub, but someone might call this function directly.

        RequestInformation requestInformation = message.getRequestInformation();
        String endpoint = requestInformation.getEndpointReference();
        if(endpoint == null){
            Log.e("ApplicationServer", "Endpoint reference not set");
            return new InternalMessage(STATUS_FAULT, null);
        }

        org.eclipse.jetty.client.api.Request request = _client.newRequest(requestInformation.getEndpointReference());
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_LENGTH, "200");

        //TODO: Handle exceptions
        try {
            Log.d("ApplicationServer", "Sending message to " + requestInformation.getEndpointReference());
            request.content(new InputStreamContentProvider((InputStream) message.getMessage()), "application/soap+xml;charset/utf-8");
            ContentResponse response = request.send();
            System.out.println("Hello");
            return new InternalMessage(STATUS_OK, response.getContentAsString());
        } catch(Exception e){
            e.printStackTrace();
            return new InternalMessage(STATUS_FAULT_INTERNAL_ERROR, null);
        }
        /* Some error has occured, return error-code TODO: Handle exceptions */
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
         * Handles an httpRequest.
         * @param s
         * @param request
         * @param httpServletRequest
         * @param httpServletResponse
         * @throws IOException
         * @throws ServletException
         */
        @Override
        public void handle(String s, org.eclipse.jetty.server.Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {

            /* Handle headers */
            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();

            while(headerNames.hasMoreElements()){
                String headerName = headerNames.nextElement();
                Enumeration<String> headers = httpServletRequest.getHeaders(headerName);

                // TODO: Here we need to handle all headers that is necessary.
                // Temporary debugging
                while(headers.hasMoreElements()){
                    System.out.println(headerName + "=" + headers.nextElement());
                }
            }

            Log.d("ApplicationServer", "Accepted message");

            /* Get content */
            if(httpServletRequest.getContentLength() > 0){
                InputStream input = httpServletRequest.getInputStream();

                /* Send the message to the hub */
                InternalMessage outMessage = new InternalMessage(STATUS_OK, input);
                outMessage.getRequestInformation().setEndpointReference(request.getRemoteHost());
                outMessage.getRequestInformation().setRequestURL(request.getRequestURI());
                outMessage.getRequestInformation().setParameters(request.getParameterMap());
                Log.d("ApplicationServer", "Forwarding message");
                InternalMessage returnMessage = ApplicationServer.this._parentHub.acceptNetMessage(outMessage);


                /* Handle possible errors */
                if((returnMessage.statusCode & STATUS_FAULT) > 0){
                    httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    request.setHandled(true);
                    return;
                //TODO: A bit unecessary perhaps? Redo into two layers?
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

                /* Something went wrong, and an error-message is being returned
                 * This is only here for theoretical reasons. Calling something like this should make you
                 * rethink your Web Service's architecture */
                }else if((STATUS_FAULT & STATUS_HAS_MESSAGE) > 0){
                    httpServletResponse.setContentType("application/soap+xml;charset=utf-8");

                    InputStream inputStream = (InputStream)returnMessage.getMessage();
                    OutputStream outputStream = httpServletResponse.getOutputStream();

                    /* google.commons helper function*/
                    ByteStreams.copy(inputStream, outputStream);

                    httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    outputStream.flush();
                    request.setHandled(true);
                /* Everything is fine, and nothing is expected */
                }else if((STATUS_OK & returnMessage.statusCode) > 0){
                    httpServletResponse.setStatus(HttpStatus.OK_200);
                    request.setHandled(true);
                }else if((STATUS_INVALID_DESTINATION & returnMessage.statusCode) > 0){
                    httpServletResponse.setStatus(HttpStatus.NOT_FOUND_404);
                    request.setHandled(true);
                }else{
                    httpServletResponse.setStatus(HttpStatus.OK_200);
                    request.setHandled(true);
                }
            }
            /* No content requested, return a 204: No content */
            else{
                httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                request.setHandled(true);
            }
        }
    }

    public static String getURI(){
        return _server.getURI().getHost()+":"+_server.getURI().getPort();
    }
}
