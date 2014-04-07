package integration;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by tormod on 22.03.14.
 */
public class TestExternalWebService {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testServer() throws Exception {
        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        Request request = client.newRequest("http://hawk.sexy:8080");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(TestExternalWebService.class.getResourceAsStream("/server_test_notify.xml")));

        ContentResponse response = request.send();
        System.out.println(response.getStatus());
    }
}
