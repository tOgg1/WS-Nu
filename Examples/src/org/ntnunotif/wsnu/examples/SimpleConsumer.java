package org.ntnunotif.wsnu.examples;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.examples.generated.IntegerContent;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * SimpleConmsumer example that takes a notification, unpacks it, and prints it.
 * @author Tormod Haugland
 * Created by tormod on 3/17/14.
 */
public class SimpleConsumer implements ConsumerListener {

    private Hub hub;
    private NotificationConsumer consumer;
    private long startTime;
    private int receivedPackages;

    public static void main(String[] args) throws Exception{
        Log.setEnableDebug(true);
        Log.initLogFile();
        XMLParser.registerReturnObjectPackageWithObjectFactory("org.ntnunotif.wsnu.examples.generated");

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
                    }else if(in.matches("^inf?o?.*?")){
                        System.out.println("Uptime: " +
                                new DecimalFormat("#.##").format((double)(System.currentTimeMillis() - startTime)/(3600*1000))
                                + " hours");
                        System.out.println("Received packages: " + receivedPackages);
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
        ++receivedPackages;
        System.out.println("I got something!");
        /* This is a org.ntnunotif.wsnu.examples.SimpleConsumer, so we just take an event, display/save its contents, and leave */

        try {
            String notifyAsXml = event.getXML();
            Log.d("SimpleConsumer", notifyAsXml);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Notify notification = event.getRaw();
        for (NotificationMessageHolderType o : notification.getNotificationMessage()) {
            System.out.println(o.getMessage().getClass());
            System.out.println(o.getMessage().toString());
            System.out.println(o.getMessage().getAny());
            System.out.println(o.getMessage().getAny().getClass());
            if(o.getMessage().getAny() instanceof IntegerContent){
                IntegerContent content = (IntegerContent) o.getMessage().getAny();
                System.out.println(content.getIntegerValue().toString());
            }
        }
    }
}
