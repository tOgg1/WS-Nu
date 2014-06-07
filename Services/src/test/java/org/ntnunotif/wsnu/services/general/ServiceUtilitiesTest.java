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

package org.ntnunotif.wsnu.services.general;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3._2001._12.soap_envelope.Envelope;
import org.w3._2001._12.soap_envelope.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.io.FileOutputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.*;

/**
 * Test for the class {@link org.ntnunotif.wsnu.services.general.ServiceUtilities}
 */
public class ServiceUtilitiesTest {

    @BeforeClass
    public static void setUp(){
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);
    }

    @Test
    public void testXsdDuration() throws Exception {
        String testOne = "P5Y2M10DT15H";
        String testTwo = "P10D";
        String testThree = "P5Y2M10DT60S";
        String testFour = "P52MT4D";
        String testFive = "P5YT3Y";

        assertTrue(ServiceUtilities.isXsdDuration(testOne));
        assertTrue(ServiceUtilities.isXsdDuration(testTwo));
        assertTrue(ServiceUtilities.isXsdDuration(testThree));
        assertTrue(!ServiceUtilities.isXsdDuration(testFour));
        assertTrue(!ServiceUtilities.isXsdDuration(testFive));
    }

    @Test
    public void testXsdDatetime() throws Exception {
        String testOne = "2002-05-30T09:00:00";
        String testTwo = "2002-05-40T09:00:00Z";
        String testThree = "2002-05-30T09:00:00-06:00";
        String testFour = "2012-09-31T23:55:00";
        String testFive = "2014-21-05T22:00:00Z";
        String testSix = "2014-11-05T41:00:00Z";
        assertTrue(ServiceUtilities.isXsdDatetime(testOne));
        assertTrue(!ServiceUtilities.isXsdDatetime(testTwo));
        assertTrue(ServiceUtilities.isXsdDatetime(testThree));
        assertTrue(ServiceUtilities.isXsdDatetime(testFour));
        assertTrue(!ServiceUtilities.isXsdDatetime(testFive));
        assertTrue(!ServiceUtilities.isXsdDatetime(testSix));
    }

    @Test
    public void testExtractXsdDur() throws Exception{
        String test="PT5H";
        long lol = ServiceUtilities.extractXsdDuration(test);
        assertTrue(lol - System.currentTimeMillis() <  1000*3600*5 + 1000*10);

        String yearTest = "P2Y9MT3H2M";
        long lol2 = ServiceUtilities.extractXsdDuration(yearTest);

        assertTrue(lol2 > 1000*3600*24*265);
    }

    @Test
    public void testExtractDateTime() throws Exception {
        String test = "2014-08-02T11:50:00";
        long lol = ServiceUtilities.extractXsdDatetime(test);
    }

    @Test
    public void testParseW3cTime() throws Exception{
        String testOne="<wsa:Address>79.120.4.2</wsa:Address>";
        String extractedOne = ServiceUtilities.parseW3CEndpoint(testOne);
        assertEquals("http://79.120.4.2", extractedOne);

        String testTwo="<Address>tormod.haugland.com:8080</Address>";
        String extractedTwo = ServiceUtilities.parseW3CEndpoint(testTwo);
        assertEquals("http://tormod.haugland.com:8080", extractedTwo);

        String testThree="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><EndpointReference xmlns=\"http:" +
                "//www.w3.org/2005/08/addressing\"><Address>78.91.27.226:8080</Address></EndpointReference>";
        String extractedThree = ServiceUtilities.parseW3CEndpoint(testThree);
        assertEquals("http://78.91.27.226:8080", extractedThree);

        String testFour="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><EndpointReference xmlns=\"http://www.w3.org/2005/08/addressing\"><Address>\n" +
                "                    78.91.27.226:8080\n" +
                "                </Address></EndpointReference>";
        String extractedFour = ServiceUtilities.parseW3CEndpoint(testFour);
        assertEquals("http://78.91.27.226:8080", extractedFour);
    }

    @Test
    public void testContentManager() throws Exception{
        ServiceUtilities.ContentManager contentManager = new ServiceUtilities.ContentManager("151.236.234");

        contentManager.addContains("porn");
        contentManager.addRegex(".*[.]html");
        contentManager.addCountLimitation("lol", 5);

        String testOne = "thislolIslolSomeXmllolFileThatShouldlolBeReturned.xml";
        String testTwo = "plain/html/that/should/be/removed.html";
        String testThree= "porn.com";
        String testFour = "lolthislolislolsomelollolroflllolsentencelol.xml";

        assertTrue(contentManager.accepts(testOne));
        assertFalse(contentManager.accepts(testTwo));
        assertFalse(contentManager.accepts(testThree));
        assertFalse(contentManager.accepts(testFour));
    }

    @Test
    public void testCreateNotify() throws Exception {

        /* Lets put an envelope in the notify, LOL */
        ObjectFactory objectFactory = new ObjectFactory();
        Envelope envelope = objectFactory.createEnvelope();
        envelope.setHeader(objectFactory.createHeader());
        envelope.setBody(objectFactory.createBody());

        Notify notify = ServiceUtilities.createNotify(envelope, "TormodHaugland.com");

        FileOutputStream file1 = new FileOutputStream(getClass().getResource("/TestNotifyOne.xml").getFile());
        XMLParser.writeObjectToStream(notify, file1);

        /* Multiple messages */
        Notify doubleNotif = ServiceUtilities.createNotify(new Envelope[]{envelope, envelope}, "TormodHaugland.com");

        FileOutputStream file2 = new FileOutputStream(getClass().getResource("/TestNotifyTwo.xml").getFile());
        XMLParser.writeObjectToStream(doubleNotif, file2);
    }

    @Test
    public void testGetAddress() throws Exception {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address("tormod.haugland.com");

        W3CEndpointReference ref = builder.build();

        String address = ServiceUtilities.getAddress(ref);

        assertEquals("tormod.haugland.com", address);

    }

    @Test
    public void testFilterendpointReference() throws Exception {
        String endpoint_1 = "http://tormod.haugland.com/lol";
        String endpoint_2 = "http://tormod.no/hei";
        String endpoint_3 = "http://127.0.0.1:8080/hei";
        String endpoint_4 = "https://127.03.42.523/tormod/webservice/lol";

        String filtered_1 = ServiceUtilities.filterEndpointReference(endpoint_1);
        String filtered_2 = ServiceUtilities.filterEndpointReference(endpoint_2);
        String filtered_3 = ServiceUtilities.filterEndpointReference(endpoint_3);
        String filtered_4 = ServiceUtilities.filterEndpointReference(endpoint_4);

        assertEquals("lol", filtered_1);
        assertEquals("hei", filtered_2);
        assertEquals("hei", filtered_3);
        assertEquals("tormod/webservice/lol", filtered_4);
    }

    @Test
    public void testSendNode() throws Exception {
        SoapForwardingHub hub = new SoapForwardingHub();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = factory.newDocumentBuilder().newDocument();
        Element element = document.createElementNS("http://example.org", "example");

        document.appendChild(element);

        ServiceUtilities.sendNode("http://127.0.0.1:8080", element, hub);
    }
}
