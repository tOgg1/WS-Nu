package org.ntnunotif.wsnu.services.general;

import org.junit.Test;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3._2001._12.soap_envelope.Envelope;

import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.TestCase.*;

/**
 * Created by tormod on 24.03.14.
 */
public class ServiceUtilitiesTest {

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
    public void testRegexOne() throws Exception{
        String test = "P1D";
        assertTrue(test.matches("^(-P|P)[0-9]D"));
        assertTrue(test.matches("^(-P|P)([0-9]D)?"));
        test = "P10Y5D";
        assertTrue(test.matches("^(-P|P)([0-9]+Y)?([0-9]+D)?"));
        assertTrue(test.matches("^(-P|P)(([0-9]+Y)?([0-9]+D)?)?(?:(T([0-9]+H)))?"));
        test = "P10YT5H";
        assertTrue(test.matches("^(-P|P)(([0-9]+Y)?([0-9]+D)?)?(?:(T([0-9]+H)))?"));
        test = "PT354H";
        assertTrue(test.matches("^(-P|P)(([0-9]+Y)?([0-9]+D)?)?(?:(T([0-9]+H)))?"));
    }

    @Test
    public void testRegexTwo() throws Exception{
        String test = "P1D";
        Matcher matcher = Pattern.compile("[0-9]+D").matcher(test);
        if(matcher.find()){
            String match = matcher.group();
            System.out.println(match);
        }
    }

    @Test
    public void testRegexThree() throws Exception{
        String test = "http://tormodhaugland.com/myWebService";
        String withoutDomain = test.replaceAll("^"+"http://tormodhaugland.com", "");
        System.out.println(withoutDomain);
        String withoutSlash = withoutDomain.replaceAll("^/", "");
        System.out.println(withoutSlash);

        String endPoint = "/myWebService/somedocument.xml";
        boolean matchTest = endPoint.matches("^/?" + test.replaceAll("^http://tormodhaugland.com", "") + "(.*)?");
        String stripped = test.replaceAll("http://tormodhaugland.com", "");
        System.out.println(stripped);
        System.out.println(endPoint.matches("/?"+stripped+"(.*?)"));
        System.out.println(matchTest);
    }

    @Test
    public void testRegexFour() throws Exception{
        String contentLimiterOne = ".*[.]xml";
        String contentLimiterTwo = "^tormod.*";

        String testOne = "tormod.xml";

        System.out.println(testOne.matches(contentLimiterOne));
        System.out.println(testOne.matches(contentLimiterTwo));

        String testTwo = "tormod.haugland.xml";
        System.out.println(testTwo.matches(contentLimiterOne));
        System.out.println(testTwo.matches(contentLimiterTwo));

        String testThree = "haugland.xml";
        System.out.println(testThree.matches(contentLimiterOne));
        System.out.println(testThree.matches(contentLimiterTwo));
    }

    @Test
    public void testRegexFive() throws Exception{
        String faultNameOne = "FaultInfo";
        String faultNameTwo = "getFaultInfo";
        String faultNameThree = "fault";
        String faultNameFour = "GiveFault";
        String faultNameFive = "info";
        String faultNameSix = "verylongmethodnamethatdoesinfacthaveinfoinit";
        String faultNameSeven= "anotherverylongmetodnamebutthisdoesnothavethewordortheotherwordwhatwasitfault?initahfuck";
        String faultNameEight = "thisdoesnotcontainanythingwelike";

        String regex = ".*((([Ff][Aa][Uu][Ll][Tt])|([Ii][Nn][Ff][Oo]))+).*";

        boolean matchesOne = faultNameOne.matches(regex);
        boolean matchesTwo = faultNameTwo.matches(regex);
        boolean matchesThree = faultNameThree.matches(regex);
        boolean matchesFour = faultNameFour.matches(regex);
        boolean matchesFive = faultNameFive.matches(regex);
        boolean matchesSix = faultNameSix.matches(regex);
        boolean matchesSeven = faultNameSeven.matches(regex);
        boolean matchesEight = faultNameEight.matches(regex);

        assertTrue(matchesOne);
        assertTrue(matchesTwo);
        assertTrue(matchesThree);
        assertTrue(matchesFour);
        assertTrue(matchesFive);
        assertTrue(matchesSix);
        assertTrue(matchesSeven);
        assertFalse(matchesEight);

    }

    @Test
    public void testExtractXsdDur() throws Exception{
        String test="PT5H";
        long lol = ServiceUtilities.extractXsdDuration(test);
        System.out.println(lol);
        System.out.println(System.currentTimeMillis());
        System.out.println(lol - System.currentTimeMillis());
        assertTrue(lol - System.currentTimeMillis() <  1000*3600*5 + 1000*10);

        String yearTest = "P2Y9MT3H2M";
        long lol2 = ServiceUtilities.extractXsdDuration(yearTest);

        System.out.println(lol2);

    }

    @Test
    public void testExtractDateTime() throws Exception {
        String test = "2014-08-02T11:50:00";
        long lol = ServiceUtilities.extractXsdDatetime(test);
        System.out.println(lol);
    }

    @Test
    public void testInterpretTerminationTime() throws Exception {
        String testOne = "2014-08-02T11:50:00";
        System.out.println(ServiceUtilities.interpretTerminationTime(testOne));
        String testTwo = "P1DT1H";
        System.out.println(ServiceUtilities.interpretTerminationTime(testTwo));
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

        System.out.println(extractedOne);
        System.out.println(extractedTwo);
        System.out.println(extractedThree);
        System.out.println(extractedFour);
    }

    public void testContentManager() throws Exception{
        ServiceUtilities.ContentManager contentManager = new ServiceUtilities.ContentManager("151.236.234");

        contentManager.addContains("porn");
        contentManager.addRegex(".*[.]html");
        contentManager.addCountLimitation("lol", 5);

        String testOne = "thislolIslolSomeXmllolFileThatShouldlolBeReturned.xml";
        String testTwo = "plain/html/that/should/be/removed.html";
        String testThree= "porn.com";
        String testFour = "lolthislolislolsomelollolroflllolsentencelol.xml";

        System.out.println(contentManager.accepts(testOne));
        System.out.println(contentManager.accepts(testTwo));
        System.out.println(contentManager.accepts(testThree));
        System.out.println(contentManager.accepts(testFour));

    }

    @Test
    public void testCreateNotify() throws Exception {

        /* Lets put an envelope in the notify, LOL */
        Envelope envelope = new Envelope();

        Notify notify = ServiceUtilities.createNotify(envelope, "TormodHaugland.com");

        FileOutputStream file1 = new FileOutputStream("Services/res/TestNotifyOne.xml");
        XMLParser.writeObjectToStream(notify, file1);

        /* Multiple messages */
        Notify doubleNotif = ServiceUtilities.createNotify(new Envelope[]{envelope, envelope}, "TormodHaugland.com");

        FileOutputStream file2 = new FileOutputStream("Services/res/TestNotifyTwo.xml");
        XMLParser.writeObjectToStream(doubleNotif, file2);
    }
}
