//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

package org.ntnunotif.wsnu.base.internal;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.jws.WebMethod;
import java.util.HashMap;

/**
 * Created by tormod on 25.03.14.
 */
public class MappingConnectorTest {
    private static MappingConnector connector;
    private static WebAwesomeService service;

    @javax.jws.WebService(name = "AwesomeService")
    private static class WebAwesomeService{

        @WebMethod
        public void myAwesomeWebMethod(String roflParameter){
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        HashMap<String, String> methodNames = new HashMap<>();
        methodNames.put("String", "myAwesomeWebMethod");

        service = new WebAwesomeService();

        connector = new MappingConnector(service, methodNames);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ApplicationServer.getInstance().stop();
    }

    @Test
    public void testAcceptMessage() throws Exception {

        Envelope env = new Envelope();
        Body body = new Body();
        body.getAny().add(new String("heeeeeyy"));
        env.setBody(body);
        InternalMessage message = new InternalMessage(InternalMessage.STATUS_OK, env);

        connector.acceptMessage(message);
    }
}
