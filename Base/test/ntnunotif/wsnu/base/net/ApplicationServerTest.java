package ntnunotif.wsnu.base.net;

import junit.framework.TestCase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.InternalHub;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;
import org.w3._2001._12.soap_envelope.Header;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by tormod on 3/6/14.
 */
public class ApplicationServerTest extends TestCase {

    private ApplicationServer _server;

    @Test
    public void testInstantiation() throws IOException {
        try{
            _server = ApplicationServer.getInstance();
            _server.start(null);
        } catch (Exception e) {
            System.err.println("Applicationserver failed to instantiate");
        }
    }

    @Test
    public void testSimpleServer() throws Exception {
        InternalHub internalHub = new InternalHub();

        // Start the server
        _server = null;
        try{
            _server = ApplicationServer.getInstance();
        } catch (Exception e) {
            System.err.println("Applicationserver failed to instantiate");
        }

        // This should not do anything, as the server is started through the internalHub (and we are working with a singleton)
        _server.start(null);

        // Start the client
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();

        // Send response
        Request request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "application");
        request.header(HttpHeader.CONTENT_LENGTH, "200");
        request.content(new InputStreamContentProvider(new FileInputStream("Base/test/ntnunotif/wsnu/base/net/server_test_html_content.html")),
                                                                           "text/html;charset/utf-8");
        ContentResponse response = request.send();
        assertEquals(500, response.getStatus());
        _server.stop();
    }

    @Test
    public void testSendingXML() throws Exception {
        InternalHub internalHub = new InternalHub();

        // Start the server
        _server = null;
        try{
            _server = ApplicationServer.getInstance();
        } catch (Exception e) {
            System.err.println("Applicationserver failed to instantiate");
        }

        // This should not do anything, as the server is started through the internalHub (and we are working with a singleton)
        _server.start(null);

        // Start the client
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();

        // Send response
        Request request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "application");
        request.header(HttpHeader.CONTENT_LENGTH, "200");
        request.content(new InputStreamContentProvider(new FileInputStream("Base/test/ntnunotif/wsnu/base/net/server_test_xml.xml")),
                "application/soap+xml;charset/utf-8");

        ContentResponse response = request.send();
        //TODO: This should be changed to some error status, as the server should not be able to process plain xml
        assertEquals(200, response.getStatus());
        _server.stop();
    }

    @Test
    public void testSendingSoap() throws Exception {
        InternalHub internalHub = new InternalHub();

        // Start the server
        _server = null;
        try{
            _server = ApplicationServer.getInstance();
        } catch (Exception e) {
            System.err.println("Applicationserver failed to instantiate");
        }

        // This should not do anything, as the server is started through the internalHub (and we are working with a singleton)
        _server.start(null);

        // Start the client
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();

        Object object = XMLParser.parse(new FileInputStream("Base/test/ntnunotif/wsnu/base/net/server_test_soap.xml"));
        Envelope env = (Envelope)object;
        Header head = env.getHeader();
        Body body = env.getBody();
        System.out.println(head);
        System.out.println(body);

        // Send response
        Request request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(new FileInputStream("Base/test/ntnunotif/wsnu/base/net/server_test_soap.xml")),
                "application/soap+xml;charset/utf-8");

        ContentResponse response = request.send();
        assertEquals(200, response.getStatus());

        //TODO: This should contain some WS error
        response.getContentAsString();

        _server.stop();
    }

    @Test
    public void testSubscribe() throws Exception {
        InternalHub internalHub = new InternalHub();

        // Start the server
        _server = null;
        try{
            _server = ApplicationServer.getInstance();
        } catch (Exception e) {
            System.err.println("Applicationserver failed to instantiate");
        }

        // This should not do anything, as the server is started through the internalHub (and we are working with a singleton)
        _server.start(null);

        // Start the client
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();

        // Send response
        FileInputStream file = new FileInputStream("Base/test/ntnunotif/wsnu/base/net/server_test_subscribe.xml");
        Object object = XMLParser.parse(file);
        Envelope env = (Envelope)object;
        Header head = env.getHeader();
        Body body = env.getBody();
        System.out.println(head);
        System.out.println(body);
        System.out.println(object);

        Request request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "application");
        request.header(HttpHeader.CONTENT_LENGTH, "200");
        request.content(new InputStreamContentProvider(file),
                "application/soap+xml;charset/utf-8");

        ContentResponse response = request.send();
        assertEquals(200, response.getStatus());

        //TODO: This should contain some WS error, unless we can process the ws:notify in server_test_soap.
        System.out.println(response.getContentAsString());

        _server.stop();
    }
}
