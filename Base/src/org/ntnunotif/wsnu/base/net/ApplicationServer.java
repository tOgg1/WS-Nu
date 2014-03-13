package org.ntnunotif.wsnu.base.net;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.ntnunotif.wsnu.base.internal.InternalHub;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


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
    private static Server _server;

    /**
     * Jetty-http client
     */
    private static HttpClient _client;

    /**
     * Thread for the server to run on
     */
    private static Thread _serverThread;

    /**
     * A bus object as parent. Needed to reroute requests to bus.
     */
    private static InternalHub _parentInternalHub;

    /**
     * Variable to check if this server is running. Primarily used to avoid double @start calls.
     */
    private static boolean _isRunning = false;

    /**
     * As this class is a singleton no external instantiation is allowed.
     */
    private ApplicationServer() throws Exception
    {
        _server = new Server(8080);
        _server.setHandler(new HttpHandler());
    }

    /**
     * Function to return the singleton instance. Will create a new instance if no instance has yet been instantiated.
     * @return Returns the running singleton instance
     */
    public static ApplicationServer getInstance() throws Exception{
        if(_singleton == null){
            _singleton = new ApplicationServer();
            return _singleton;
        }else{
            return _singleton;
        }
    }

    /**
     * Start the http-server.
     * @throws java.lang.Exception Throws an exception if the server is unable to stop.
     */
    public static void start(InternalHub internalHub) throws Exception{
        if(_isRunning){
            return;
        }

        _isRunning = true;
        _parentInternalHub = internalHub;
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
    public static void stop(){
        try {
            _server.stop();
            _serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes a message as an inputStream and sends it to a recipient over HTML. This function expects a response,
     * and sends this response back up the system.
     * @param inputStream
     * @param recipient
     * @return An array with <code>{int status, InputStream contentRecieved}</code>
     */
    public static Object[] sendMessage(InputStream inputStream, String recipient){
        org.eclipse.jetty.client.api.Request request = _client.newRequest(recipient);
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_LENGTH, "200");
        request.content(new InputStreamContentProvider(inputStream),
                "application/soap+xml;charset/utf-8");
        //TODO: Handle exceptions
        try {
            ContentResponse response = request.send();
            return new Object[]{response.getStatus(), new ByteArrayInputStream(response.getContent())};
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        /* Some error has occured, return error-code TODO: Handle exceptions */
        return new Object[]{HttpStatus.NOT_FOUND_404, null};
    }

    /**
     * Inner class to handle http-requests.
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

            /* Get content */
            if(httpServletRequest.getContentLength() > 0){
                InputStream input = httpServletRequest.getInputStream();

                ApplicationServer.this._parentInternalHub.acceptNetMessage(input);
            }
            /* No content found, return a 204: No content */
            else{
                httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                request.setHandled(true);
            }

            // Temporary for testing purposes
            httpServletResponse.setContentType("text/html;charset=utf-8");
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            request.setHandled(true);
            httpServletResponse.getWriter().println("<h1>Hello world</h1>");
        }
    }
}
