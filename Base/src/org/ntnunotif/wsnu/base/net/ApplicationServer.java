package org.ntnunotif.wsnu.base.net;

import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    public static void start() throws Exception{
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
         * Handles an httpRequest
         * @param s
         * @param request
         * @param httpServletRequest
         * @param httpServletResponse
         * @throws IOException
         * @throws ServletException
         */
        @Override
        public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {

            System.out.println(httpServletRequest.getContentType());
            System.out.println(httpServletRequest.getAuthType());
            System.out.println(httpServletRequest.getMethod());
            System.out.println(httpServletRequest.getQueryString());

            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();

            while(headerNames.hasMoreElements()){
                Enumeration<String> headers = httpServletRequest.getHeaders(headerNames.nextElement());

                while(headers.hasMoreElements()){
                    System.out.println(headers.nextElement());
                }
            }

            httpServletResponse.setContentType("text/html;charset=utf-8");
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            request.setHandled(true);
            httpServletResponse.getWriter().println("<h1>Hello world</h1>");
        }
    }
}
