package org.ntnunotif.wsnu.examples;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.oasis_open.docs.wsn.b_2.Subscribe;

import javax.management.Notification;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * SimpleConmsumer example that takes a notification, unpacks it, and prints it.
 * @author Tormod Haugland
 * Created by tormod on 3/17/14.
 */
public class SimpleConsumer implements ConsumerListener {

    private Hub hub;
    private NotificationConsumer consumer;
    private long startTime;

    public static void main(String[] args) throws Exception{
        Log.setEnableDebug(true);

        ApplicationServer.useConfigFile = false;
        ApplicationServer appServer = ApplicationServer.getInstance();
        Server server = appServer.getServer();

        /* Configure server without file and with multiple connectors */
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

        SimpleConsumer simpleConsumer = new SimpleConsumer();
    }

    public void sendSubscriptionRequest(String address){
        consumer.sendSubscriptionRequest(address);
    }

    public SimpleConsumer() {
        consumer = new NotificationConsumer();
        hub = consumer.quickBuild();
        consumer.setEndpointReference("Hello");
        consumer.addConsumerListener(this);
        startTime = System.currentTimeMillis();

        InputManager in = new InputManager();
        in.start();
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
                    if(in.matches("^exit")) {
                        System.exit(0);
                    }else if(in.matches("^info")){
                        Log.d("SimpleConsumer", "INFO\n------\nUptime: " +
                                new DecimalFormat("#.##").format((double)(System.currentTimeMillis() - startTime)/(3600*1000))
                                + " hours");
                    }else if(in.matches("^subscribe *[0-9a-zA-Z.:/]+")){
                        String address = in.replaceAll("^subscribe", "").replaceAll(" ", "");
                        Log.d("SimpleConsumer", "Parsed endpointreference: " + address);
                        System.out.println(address.replaceAll("^http://.*?", ""));

                        if(!address.matches(("^https?://.*?"))){
                            Log.d("SimpleConsumer", "Inserted http://-tag");
                            address = "http://" + address;
                        }

                        SimpleConsumer.this.sendSubscriptionRequest(address);
                    }else{
                        Log.d("SimpleConsumer", "Command not supported");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    @Override
    public void notify(NotificationEvent event) {
        /* This is a org.ntnunotif.wsnu.examples.SimpleConsumer, so we just take an event, display its contents, and leave */
        Notify notification = event.getRaw();

        List<Object> everything = notification.getAny();

        for (Object o : everything) {
            System.out.println(o.getClass());
            System.out.println(o);
        }
    }
}
