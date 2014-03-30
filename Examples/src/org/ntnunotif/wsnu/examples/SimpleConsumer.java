package org.ntnunotif.wsnu.examples;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.examples.generated.IntegerContent;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;

import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
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

    public SimpleConsumer() throws NoSuchMethodException {

        consumer = new NotificationConsumer();
        hub = consumer.quickBuild();
        consumer.setEndpointReference("Hello");
        consumer.addConsumerListener(this);
        startTime = System.currentTimeMillis();

        /* Creates an inputManager and registers some commands it wants rerouted */
        ServiceUtilities.InputManager inputManager = new ServiceUtilities.InputManager();

        /* Reroutes matches of ^inf?o?.*? to the function handleInfo using regex */
        inputManager.addMethodReroute("info", "^inf?o?.*?", true, this.getClass().getMethod("handleInfo", String.class), this);
        inputManager.addMethodReroute("subscribe", "^subscribe *[0-9a-zA-Z.:/]+", true, this.getClass().getMethod("handleSubscribe", String.class), this);
        inputManager.addMethodReroute("request", "^request (.*)+", true, this.getClass().getMethod("handleRequest", String.class), this);
        inputManager.addMethodReroute("exit", "^exit", true, System.class.getDeclaredMethod("exit", Integer.TYPE), this, new ServiceUtilities.Tuple[]{new ServiceUtilities.Tuple(0, 0)});
        inputManager.start();
    }

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

        Subscribe subscriptionRequest = consumer.baseFactory.createSubscribe();

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(consumer.getEndpointReference());
        subscriptionRequest.setConsumerReference(builder.build());
        subscriptionRequest.setInitialTerminationTime(consumer.baseFactory.createSubscribeInitialTerminationTime("P1D"));

        InternalMessage returnMessage = consumer.sendSubscriptionRequest(subscriptionRequest, address);
        System.out.println(returnMessage.getMessage().toString());
    }

    public void sendRequest(String raw){
        InternalMessage message = consumer.sendRequest(raw);
        System.out.println(message.getMessage());
        System.out.println(message.getRequestInformation().toString());
    }

    public void handleInfo(String command) {
        System.out.println("Endpoint: " + SimpleConsumer.this.consumer.getEndpointReference());
        System.out.println("Uptime: " +
                new DecimalFormat("#.##").format((double)(System.currentTimeMillis() - startTime)/(3600*1000))
                + " hours");
    }

    public void handleSubscribe(String command){
        String address = command.replaceAll("^subscribe", "").replaceAll(" ", "");
        Log.d("SimpleConsumer", "Parsed endpointreference: " + address);
        System.out.println(address.replaceAll("^http://.*?", ""));

        if (!address.matches("^https?://.*?")){
            Log.d("SimpleConsumer", "Inserted http://-tag");
            address = "http://" + address;
        }

        SimpleConsumer.this.sendSubscriptionRequest(address);
    }

    public void handleRequest(String command){
        Log.d("SimpleConsumer", "Matches request");
        String raw = command.replaceAll("^request", "");
        raw = raw.replaceFirst(" ", "");


        if(!raw.matches("^https?://.*?")){
            Log.d("SimpleConsumer", "Inserted http://-tag");
            raw = "http://"+raw;
        }
        InternalMessage message = consumer.sendRequest(raw);
        System.out.println(message.getMessage());
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
