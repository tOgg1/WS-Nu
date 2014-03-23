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
import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;
import org.w3._2001._12.soap_envelope.Header;

import javax.xml.bind.JAXBElement;
import java.io.FileInputStream;

/**
 * Created by tormod on 3/6/14.
 */
public class ApplicationServerTest extends TestCase {

    private ApplicationServer _server;

    public void setUp() throws Exception {
        _server = ApplicationServer.getInstance();
        _server.start(new ForwardingHub());
    }

    @Test
    public void testSimpleServer() throws Exception {

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
        request.content(new InputStreamContentProvider(new FileInputStream("Base/testres/server_test_html_content.html")),
                                                                           "text/html;charset/utf-8");
        ContentResponse response = request.send();
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testSendingXML() throws Exception {

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
        request.content(new InputStreamContentProvider(new FileInputStream("Base/testres/server_test_xml.xml")),
                "application/soap+xml;charset/utf-8");

        ContentResponse response = request.send();
        //TODO: This should be changed to some error status, as the server should not be able to process plain xml
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testSendingSoap() throws Exception {
        ForwardingHub forwardingHub = new ForwardingHub();

        // Start the client
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();

        Object object = XMLParser.parse(new FileInputStream("Base/testres/server_test_soap.xml"));
        Envelope env = (Envelope)((JAXBElement)((InternalMessage) object).get_message()).getValue();
        Header head = env.getHeader();
        Body body = env.getBody();
        System.out.println(head);
        System.out.println(body);

        // Send response
        Request request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(new FileInputStream("Base/testres/server_test_soap.xml")),
                "application/soap+xml;charset/utf-8");

        ContentResponse response = request.send();
        assertEquals(200, response.getStatus());

        //TODO: This should contain some WS error
        response.getContentAsString();

    }

    @Test
    public void testSubscribe() throws Exception {

        // Start the client
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();

        // Send response
        FileInputStream file = new FileInputStream("Base/testres/server_test_subscribe.xml");


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
    }

    public void tearDown() throws Exception {
        super.tearDown();
        _server.stop();
    }
}
