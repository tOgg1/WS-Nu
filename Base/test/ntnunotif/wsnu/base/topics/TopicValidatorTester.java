package ntnunotif.wsnu.base.topics;

import org.ntnunotif.wsnu.base.net.XMLParser;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * <code>TopicValidatorTester</code> tests the <code>TopicValidator</code>. It is dependent on <code>XMLParser</code>
 * Created by Inge on 06.03.14.
 */
public class TopicValidatorTester {

    public static void main(String[] args) {
        File file = new File("Base/test/ntnunotif/wsnu/base/net/parse_test_notify.xml");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Object parsedObject = XMLParser.parse(fileInputStream);
            // TODO Write a testing xml file and some testing code
        } catch (FileNotFoundException e) {
            System.out.println("topic test parse file not found.");
            e.printStackTrace();
        } catch (JAXBException e) {
            System.out.println("Could not parse xml file");
            e.printStackTrace();
        }
    }
}
