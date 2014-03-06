package ntnunotif.wsnu.base.net;

import junit.framework.TestCase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.ApplicationServer;

import java.io.FileInputStream;

/**
 * Created by tormod on 3/6/14.
 */
public class ApplicationServerTest extends TestCase {

    private ApplicationServer _server;

    @Test
    public void testInstantiation(){
        try{
            ApplicationServer applicationServer = ApplicationServer.getInstance();
        } catch (Exception e) {
            System.err.println("Applicationserver failed to instantiate");
        }
    }

    @Test
    public void testSimpleServer() throws Exception {
        // Start the server
        _server = null;
        try{
            _server = ApplicationServer.getInstance();
        } catch (Exception e) {
            System.err.println("Applicationserver failed to instantiate");
        }

        _server.start();

        // Start the client
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();


        // Send response
        Request request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "text/plain");
        request.header(HttpHeader.CONTENT_LENGTH, "200");
        request.content(new InputStreamContentProvider(new FileInputStream("Base/test/ntnunotif/wsnu/base/net/server_test_html_content.html")), "text/plain");


        ContentResponse response = request.send();

        assertEquals(200, response.getStatus());
        _server.stop();
    }

    @Test
    public void testSendingXML() throws Exception {
        _server = null;
        try{
            _server = ApplicationServer.getInstance();
        } catch (Exception e) {
            System.err.println("Applicationserver failed to instantiate");
        }

        _server.start();

        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(false);
        client.start();



    }

    @Test
    public void testSendingSoap() throws Exception {

    }

    @Test
    public void testWebService() throws Exception {

    }
}
