package org.ntnunotif.wsnu.examples;

import org.ntnunotif.wsnu.examples.generated.IntegerContent;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.SimpleNotificationProducer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

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
        this.simpleNotificationProducer.setEndpointReference("numberProducer");
        InputManager in = new InputManager();
        in.start();
    }

    /**
     * Send some data
     * @param data
     */
    public void sendNotification(int data){
        Notify notify = new Notify();

        NotificationMessageHolderType type = new NotificationMessageHolderType();
        ObjectFactory factory = new ObjectFactory();
        NotificationMessageHolderType.Message message = new NotificationMessageHolderType.Message();
        IntegerContent content = new IntegerContent();
        BigInteger integer = BigInteger.valueOf(data);
        content.setInteger(integer);
        message.setAny(content);
        type.setMessage(message);

        notify.getNotificationMessage().add(type);
        hub.acceptLocalMessage(new InternalMessage(STATUS_OK| STATUS_HAS_MESSAGE, notify));
        simpleNotificationProducer.sendNotification(notify);
    }

    public static void main(String[] args) {
        Log.initLogFile();
        Log.setEnableDebug(true);

        XMLParser.registerReturnObjectPackageWithObjectFactory("org.ntnunotif.wsnu.examples.generated");

        SimpleNumberProducer producer = new SimpleNumberProducer();
        producer.start();
    }

    private class InputManager{

        public InputManager(){

        }

        public void start(){
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Inputmanager started...\n");
            String in;
            try {
                while((in = reader.readLine()) != null){
                    if(in.matches("^exit")){
                        System.exit(0);
                    }else if(in.matches("^notify *[0-9]+")) {

                        int data = Integer.parseInt(in.replaceAll(" ", "").replaceAll("^notify", ""));
                        SimpleNumberProducer.this.sendNotification(data);
                    }else if(in.matches("^info")){
                        System.out.println(SimpleNumberProducer.this.simpleNotificationProducer.getEndpointReference());
                    }else{
                        System.out.println("Command not supported");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }

    }

}
