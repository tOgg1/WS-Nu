import com.sun.xml.internal.messaging.saaj.soap.impl.TextImpl;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.SimpleNotificationProducer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import sun.net.www.http.HttpClient;

import javax.xml.soap.Node;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;

/**
 * Created by tormod on 25.03.14.
 */
public class SimpleNumberProducer {

    private SimpleNotificationProducer simpleNotificationProducer;
    private Hub hub;

    public SimpleNumberProducer() {
        this.simpleNotificationProducer = new SimpleNotificationProducer();
    }

    public void start(){
        this.hub = simpleNotificationProducer.quickBuild();
        this.simpleNotificationProducer.setEndpointReference("myProducer");
        InputManager in = new InputManager();
    }

    /**
     * Send some data
     * @param data
     */
    public void sendNotification(Integer data){
        Notify notify = new Notify();
        simpleNotificationProducer.sendNotification(notify);
    }

    public static void main(String[] args) {
        Log.initLogFile();
        Log.setEnableDebug(true);

        SimpleNumberProducer producer = new SimpleNumberProducer();
        producer.start();
    }

    private class InputManager{

        public InputManager(){

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Inputmanager started...\n");
            String in;
            try {
                while((in = reader.readLine()) != null){
                    if(in.matches("^exit")){
                        System.exit(0);
                    }else if(in.matches("^notify *[0-9]+")) {

                        Integer data = Integer.parseInt(in.replaceAll(" ", "").replaceAll("^notify", ""));
                        SimpleNumberProducer.this.sendNotification(data);
                    }else if(in.matches("^info")){
                        System.out.println(SimpleNumberProducer.this.simpleNotificationProducer.getEndpointReference());
                    }else{
                        System.out.println("Command not supported");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
