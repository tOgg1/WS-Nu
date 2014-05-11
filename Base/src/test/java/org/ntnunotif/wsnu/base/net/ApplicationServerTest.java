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

package org.ntnunotif.wsnu.base.net;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;
import org.w3._2001._12.soap_envelope.Header;

import javax.xml.bind.JAXBElement;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by tormod on 3/6/14.
 */
public class ApplicationServerTest {

    private static ApplicationServer _server;

    @BeforeClass
    public static void setUp() throws Exception {
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);
        _server = ApplicationServer.getInstance();
        _server.start(new SoapForwardingHub());
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
        request.content(new InputStreamContentProvider(getClass().getResourceAsStream("/server_test_html_content.html")),
                                                                           "text/html;charset/utf-8");
        ContentResponse response = request.send();
        assertEquals("Response on bad request was not 500", 500, response.getStatus());
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
        request.content(new InputStreamContentProvider(getClass().getResourceAsStream("/server_test_xml.xml")),
                "application/soap+xml;charset/utf-8");

        ContentResponse response = request.send();
        assertEquals("Response on bad request was not 500", 500, response.getStatus());
    }

    @Test
    public void testSendingSoap() throws Exception {
        SoapForwardingHub soapForwardingHub = new SoapForwardingHub();

        // Start the client
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();

        Object object = XMLParser.parse(getClass().getResourceAsStream("/server_test_soap.xml"));
        Envelope env = (Envelope)((JAXBElement)((InternalMessage) object).getMessage()).getValue();
        Header head = env.getHeader();
        Body body = env.getBody();

        // Send response
        Request request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(getClass().getResourceAsStream("/server_test_soap.xml")),
                "application/soap+xml;charset/utf-8");

        ContentResponse response = request.send();
        assertEquals("Expected not found", 404, response.getStatus());
    }

    @Test
    public void testSubscribe() throws Exception {

        // Start the client
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();

        // Send response
        InputStream file = getClass().getResourceAsStream("/server_test_subscribe.xml");

        Request request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "application");
        request.header(HttpHeader.CONTENT_LENGTH, "200");
        request.content(new InputStreamContentProvider(file),
                "application/soap+xml;charset/utf-8");

        ContentResponse response = request.send();
        assertEquals("Expected not found", 404, response.getStatus());
    }


    @AfterClass
    public static void tearDown() throws Exception {
        _server.stop();
    }
}
