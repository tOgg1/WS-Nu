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

package org.ntnunotif.wsnu.base.util;

import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Test for the class {@link org.ntnunotif.wsnu.base.util.Utilities}
 */
public class UtilitiesTest {

    @Test
    public void testCountOccurences() throws Exception {
        String someString = "hithisisastringwithhihi";

        assertEquals("Expected count of 'hi's in string was wrong", 4, Utilities.countOccurences(someString, "hi"));
    }


    @Test
    public void testPlainIPv4AndIPv6() throws Exception {
        String legalIPv6_one = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        String legalIPv6_two = "[2001:db8:85a3:0:0:8a2e:370:7334]";
        String legalIPv6_three = "2001:db8:85a3::8a2e:370:7334";
        String legalIPv6_four = "::";
        String legalIPv6_five = "::1";
        String legalIPv6_six = "::ffff:127.0.0.1";

        String legalIPv4_one = "127.0.0.1";
        String legalIPv4_two = "255.255.0.0";

        String illegalIPv6_one = "2001:0db8:85a3:0000:0000:8a2e:0370";
        String illegalIPv6_two = "2001:0db8:85a3:0000:0000:8h2e:0370:0232";

        String illegalIPv4_one = "126.256.0.1";
        String illegalIPv4_two = "0.0.3";
        String illegalIPv4_three = "12.1.3.41.4";

        assertTrue(Utilities.isValidIpv6Address(legalIPv6_one));
        assertTrue(Utilities.isValidIpv6Address(legalIPv6_two));
        assertTrue(Utilities.isValidIpv6Address(legalIPv6_three));
        assertTrue(Utilities.isValidIpv6Address(legalIPv6_four));
        assertTrue(Utilities.isValidIpv6Address(legalIPv6_five));
        assertTrue(Utilities.isValidIpv6Address(legalIPv6_six));

        assertTrue(Utilities.isValidIpv4Address(legalIPv4_one));
        assertTrue(Utilities.isValidIpv4Address(legalIPv4_two));

        assertFalse(Utilities.isValidIpv6Address(illegalIPv6_one));
        assertFalse(Utilities.isValidIpv6Address(illegalIPv6_two));

        assertFalse(Utilities.isValidIpv4Address(illegalIPv4_one));
        assertFalse(Utilities.isValidIpv4Address(illegalIPv4_two));
        assertFalse(Utilities.isValidIpv4Address(illegalIPv4_three));

    }

    @Test
    public void testValidUrl() throws Exception {
        String validUrl_one = "http://example.com";
        String validUrl_two = "http://127.0.0.1:8080";

        assertTrue(Utilities.isValidUrl(validUrl_one));
        assertTrue(Utilities.isValidUrl(validUrl_two));

    }

    @Test
    public void testIPv4AndIPv6WithHttpAndPrefix() throws Exception {
        String legalIPv6_one = "http://[2001:db8:85a3::8a2e:370:7334]";
        String legalIPv6_two = "http://2001:db8:85a3::8a2e:370:7334/test/example/folder";
        String legalIPv6_three = "2001:db8:85a3::8a2e:370:7334/lol";

        String legalIPv4_one = "http://127.0.0.1";
        String legalIPv4_two = "http://121.32.32.122/lol/test";

        assertTrue(Utilities.isValidIpv4Address(legalIPv4_one));
        assertTrue(Utilities.isValidIpv4Address(legalIPv4_two));

        assertTrue(Utilities.isValidIpv6Address(legalIPv6_one));
        assertTrue(Utilities.isValidIpv6Address(legalIPv6_two));
        assertTrue(Utilities.isValidIpv6Address(legalIPv6_three));
    }

    @Test
    public void testDomain() throws Exception {
        String domainUrlLegal_one = "http://tormod.haugland.com";
        String domainUrlLegal_two = "https://catz.com/cheezeburger";

        String domainUrlIllegal_one = "http://[2001:db8:85a3::8a2e:370:7334]";
        String domainUrlIllegal_two = "http://127.0.0.1";

        assertTrue(Utilities.isValidDomainUrl(domainUrlLegal_one));
        assertTrue(Utilities.isValidDomainUrl(domainUrlLegal_two));

        assertFalse(Utilities.isValidDomainUrl(domainUrlIllegal_one));
        assertFalse(Utilities.isValidDomainUrl(domainUrlIllegal_two));
    }

    @Test
    public void testStripUrl() throws Exception {
        String preIpv6 = "http://[2001:db8:85a3::8a2e:370:7334]:8080/erg";
        String preIpv4 = "https://126.0.0.1/lol";
        String preDomain = "http://example.com/test";

        String postIpv6_one = Utilities.stripOfHost(preIpv6);
        String postIpv4_one = Utilities.stripOfHost(preIpv4);
        String postIpv6_two = Utilities.stripUrlOfProtocolAndHost(preIpv6);
        String postIpv4_two = Utilities.stripUrlOfProtocolAndHost(preIpv4);
        String postDomain_one = Utilities.stripOfHost(preDomain);
        String postDomain_two = Utilities.stripUrlOfProtocolAndHost(preDomain);

        Assert.assertEquals("http:///erg", postIpv6_one);
        Assert.assertEquals("/erg", postIpv6_two);
        Assert.assertEquals("https:///lol", postIpv4_one);
        Assert.assertEquals("/lol", postIpv4_two);
        Assert.assertEquals("http:///test", postDomain_one);
        Assert.assertEquals("/test", postDomain_two);
    }
}
