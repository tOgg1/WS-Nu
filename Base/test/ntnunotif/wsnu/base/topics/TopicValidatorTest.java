package ntnunotif.wsnu.base.topics;

import org.junit.Test;
import org.ntnunotif.wsnu.base.net.XMLParser;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * <code>TopicValidatorTest</code> tests the <code>TopicValidator</code>. It is dependent on <code>XMLParser</code>
 * Created by Inge on 06.03.14.
 */
public class TopicValidatorTest {

    @Test
    public void testTopicValidation() throws Exception{
        File file = new File("Base/test/ntnunotif/wsnu/base/net/parse_test_notify.xml");
        FileInputStream fileInputStream = new FileInputStream(file);
        Object parsedObject = XMLParser.parse(fileInputStream);
        // TODO Write a testing xml file and some testing code
    }
}
