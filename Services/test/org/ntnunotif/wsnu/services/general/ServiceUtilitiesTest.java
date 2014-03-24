package org.ntnunotif.wsnu.services.general;

import junit.framework.TestCase;
import org.junit.Test;

import javax.xml.ws.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tormod on 24.03.14.
 */
public class ServiceUtilitiesTest extends TestCase {

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

    public void testExtractDateTime() throws Exception {
        String test = "2014-08-02T11:50:00";
        long lol = ServiceUtilities.extractXsdDatetime(test);
        System.out.println(lol);
    }

    public void testInterpretTerminationTime() throws Exception {
        String testOne = "2014-08-02T11:50:00";
        System.out.println(ServiceUtilities.interpretTerminationTime(testOne));
        String testTwo = "P1DT1H";
        System.out.println(ServiceUtilities.interpretTerminationTime(testTwo));
    }
}
