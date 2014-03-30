package org.ntnunotif.wsnu.examples;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.examples.generated.IntegerContent;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.SimpleNotificationProducer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.math.BigInteger;
import java.text.DecimalFormat;

/**
 * Created by tormod on 25.03.14.
 */
public class SimpleNumberProducer {

    private SimpleNotificationProducer simpleNotificationProducer;
    private Hub hub;
    private long startTime;

    public SimpleNumberProducer() {
        this.simpleNotificationProducer = new SimpleNotificationProducer();
    }

    public void start() throws Exception{
        this.hub = simpleNotificationProducer.quickBuild();
        this.simpleNotificationProducer.setEndpointReference("numberProducer");
        this.simpleNotificationProducer.setWsdlLocation("numberProducer/SimpleNotificationProducerService.wsdl");
        startTime = System.currentTimeMillis();
        ServiceUtilities.InputManager inputManager = new ServiceUtilities.InputManager();
        inputManager.addMethodReroute("info", "^inf?o?.*?", true, this.getClass().getMethod("handleInfo", String.class), this);
        inputManager.addMethodReroute("generate", "^generate(.*)?", true, this.getClass().getMethod("handleGenerate", String.class), this);
        inputManager.addMethodReroute("notify", "^notify *[0-9]+", true, this.getClass().getMethod("handleNotify", String.class), this);
        inputManager.addMethodReroute("exit", "^exit", true, System.class.getDeclaredMethod("exit", Integer.TYPE), this, new ServiceUtilities.Tuple[]{new ServiceUtilities.Tuple(0, 0)});
        inputManager.start();
    }

    /**
     * Send some data
     * @param data
     */
    public void sendNotification(long data){
        Notify notify = new Notify();

        NotificationMessageHolderType type = new NotificationMessageHolderType();

        NotificationMessageHolderType.Message message = new NotificationMessageHolderType.Message();
        IntegerContent content = new IntegerContent();
        BigInteger integer = BigInteger.valueOf(data);
        content.setIntegerValue(integer);
        message.setAny(content);
        type.setMessage(message);
        notify.getNotificationMessage().add(type);

        simpleNotificationProducer.sendNotification(notify);
    }

    public static void main(String[] args) throws Exception {
        Log.initLogFile();
        Log.setEnableDebug(true);

        XMLParser.registerReturnObjectPackageWithObjectFactory("org.ntnunotif.wsnu.examples.generated");

        SimpleNumberProducer producer = new SimpleNumberProducer();
        producer.start();
    }

    public void handleInfo(String command){
        System.out.println("Endpoint: " + SimpleNumberProducer.this.simpleNotificationProducer.getEndpointReference());
        System.out.println("INFO\n------\nUptime: " +
                new DecimalFormat("#.##").format((double) (System.currentTimeMillis() - startTime) / (3600 * 1000))
                + " hours");
    }

    public void handleNotify(String command) {
        long data = Long.parseLong(command.replaceAll(" ", "").replaceAll("^notify", ""));
        SimpleNumberProducer.this.sendNotification(data);
    }

    public void handleGenerate(String command) {
        try {
            simpleNotificationProducer.generateWSDLandXSDSchemas();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Couldn't generate wsdl files: " + e.getMessage());
        }
    }
}
