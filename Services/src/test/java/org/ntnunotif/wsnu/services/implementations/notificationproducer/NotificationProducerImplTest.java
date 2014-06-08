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

package org.ntnunotif.wsnu.services.implementations.notificationproducer;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.ApplicationServer;

/**
 *
 */
public class NotificationProducerImplTest {

    private NotificationProducerImpl defaultProducer;
    private NotificationProducerImpl noFilterProducer;
    private NotificationProducerImpl noFilterNoCachingProducer;

    @Before
    public void setUp() {
        defaultProducer = new NotificationProducerImpl();
        noFilterProducer = new NotificationProducerImpl();
        noFilterProducer.setFilterSupport(null);
        noFilterProducer.setCacheMessages(true);
        noFilterNoCachingProducer = new NotificationProducerImpl();
        noFilterNoCachingProducer.setFilterSupport(null);
        noFilterNoCachingProducer.setCacheMessages(false);
    }

    @AfterClass
    public static void tearDown() throws Exception{
        ApplicationServer.getInstance().stop();
    }

    @Test
    public void testGetAllRecipients() throws Exception {
        // TODO
    }

    @Test
    public void testGetRecipientFilteredNotify() throws Exception {
        // TODO
    }

    @Test
    public void testSendNotification() throws Exception {
        // TODO
    }

    @Test
    public void testSubscribe() throws Exception {
        // TODO
    }

    @Test
     public void testGetCurrentMessageDefault() throws Exception {
        // TODO
    }
    /*
    @Test(expected = NoCurrentMessageOnTopicFault.class)
    public void testGetCurrentMessageNoFilter() throws Exception {
        // TODO
    }

    @Test(expected = NoCurrentMessageOnTopicFault.class)
    public void testGetCurrentMessageNoCaching() throws Exception {
        // TODO
    }
*/
/*    @Test
    public void testQuickBuild() throws Exception {
        SoapForwardingHub defaultHub  = defaultProducer.quickBuild("default");
        SoapForwardingHub noFilterHub = noFilterProducer.quickBuild("no_filter");
        SoapForwardingHub noFilterNoCacheHub = noFilterNoCachingProducer.quickBuild("no_filter_no_cache");

        Assert.assertNotNull("Default hub was null", defaultHub);
        Assert.assertNotNull("No filter hub was null", noFilterHub);
        Assert.assertNotNull("No filter and no cache hub was null", noFilterNoCacheHub);
    }*/
}
