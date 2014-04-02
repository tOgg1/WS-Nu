package org.ntnunotif.wsnu.services.general;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import javax.xml.bind.JAXBElement;
import java.io.FileInputStream;

/**
 * Created by Inge on 02.04.2014.
 */
public class FilterTest {
    private static final String subscribeWithFilterLocation = "Services/res/server_test_subscribe.xml";

    private static InternalMessage subscribeInternalMessage;
    private static FilterType filterType;

    @BeforeClass
    public static void globalSetup() {
        // read in data from files
        try {
            subscribeInternalMessage = XMLParser.parse(new FileInputStream(subscribeWithFilterLocation));
            JAXBElement<Envelope> element = (JAXBElement<Envelope>)subscribeInternalMessage.getMessage();
            Body b = element.getValue().getBody();
            filterType = ((Subscribe)b.getAny().get(0)).getFilter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExamineFilterContent() {
        if (filterType == null) {
            Assert.assertTrue("Filter was null, cannot inspect", false);
        }
        for (Object o : filterType.getAny()) {
            System.out.println(o.getClass());
        }
    }
}
