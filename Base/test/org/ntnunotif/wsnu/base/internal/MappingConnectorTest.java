package org.ntnunotif.wsnu.base.internal;

import junit.framework.TestCase;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.HashMap;

/**
 * Created by tormod on 25.03.14.
 */
public class MappingConnectorTest extends TestCase {
    MappingConnector connector;
    WebAwesomeService service;

    @javax.jws.WebService(name = "AwesomeService")
    private class WebAwesomeService{

        @WebMethod
        public void myAwesomeWebMethod(String roflParameter){
            System.out.println("HELLO!!!!!!!!!!!");
        }

    }

    public void setUp() throws Exception {
        super.setUp();

        HashMap<String, String> methodNames = new HashMap<>();
        methodNames.put("String", "myAwesomeWebMethod");

        service = new WebAwesomeService();

        connector = new MappingConnector(service, methodNames);

    }

    public void tearDown() throws Exception {

    }

    public void testAcceptMessage() throws Exception {

        Envelope env = new Envelope();
        Body body = new Body();
        body.getAny().add(new String("heeeeeyy"));
        env.setBody(body);
        InternalMessage message = new InternalMessage(InternalMessage.STATUS_OK, env);

        connector.acceptMessage(message);
    }
}
