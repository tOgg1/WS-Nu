package integration;

import junit.framework.TestCase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;

import java.io.FileInputStream;

/**
 * Created by tormod on 22.03.14.
 */
public class TestExternalWebService extends TestCase{

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testServer() throws Exception {
        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        Request request = client.newRequest("http://hawk.sexy:8080");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(new FileInputStream("IntegrationTesting/res/")));

        ContentResponse response = request.send();
        System.out.println(response.getStatus());
    }
}
