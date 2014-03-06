package org.ntnunotif.wsnu.base.net;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.ntnunotif.wsnu.base.internal.Bus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;


/**
 * Created by tormod on 3/3/14.
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
     * Thread for the server to run on
     */
    private static Thread _serverThread;

    /**
     * A bus object as parent. Needed to reroute requests to bus.
     */
    private static Bus _parentBus;

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
    public static void start(Bus bus) throws Exception{
        if(_isRunning){
            return;
        }

        _isRunning = true;
        _parentBus = bus;

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
     * Class to handle http-requests.
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
        public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {

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
            if(httpServletRequest.getContentLength() != 0){

                InputStream input = httpServletRequest.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                /* For temporary debugging */
                char[] content;
                content = new char[httpServletRequest.getContentLength()];

                reader.read(content);
                System.out.println(content);
                /**/

                ApplicationServer.this._parentBus.acceptNetMessage(input);
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
