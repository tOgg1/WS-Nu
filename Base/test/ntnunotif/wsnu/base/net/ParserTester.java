package ntnunotif.wsnu.base.net;

import org.ntnunotif.wsnu.base.net.XMLParser;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Tests the parser implementation in a very simple manner.
 * @author Inge Edward Halsaunet
 * Created by Inge on 06.03.14.
 */
public class ParserTester {
    public static void main(String[] args) {
        File file = new File("Base/test/ntnunotif/wsnu/base/net/parse_test_notify.xml");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Object parsedObj = XMLParser.parse(fileInputStream);
            if (parsedObj == null) {
                System.err.println("Parser returned null value.");
            } else if (parsedObj.getClass() != Notify.class) {
                System.err.println("Parser returned wrong object type;");
                System.err.println("\t" + Notify.class.toString() + " expected");
                System.err.println("\t" + parsedObj.getClass().toString() + " found");
            } else {
                Notify message = (Notify)parsedObj;
                System.out.println("Printing content of notify message");
                System.out.println("\tAny:");
                for (Object obj : message.getAny())
                    System.out.println("\t\t" + obj);
                System.out.println("\tNotificationMessage:");
                for (NotificationMessageHolderType notMess: message.getNotificationMessage())
                    System.out.println("\t\t"+notMess.toString());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not locate XML file.");
            e.printStackTrace();
        } catch (JAXBException e) {
            System.err.println("Could not parse XML file");
            e.printStackTrace();
        }
    }
}
