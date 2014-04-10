package org.ntnunotif.wsnu.integrationtesting;


import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;

/**
 * Created by Inge on 09.04.2014.
 */
public class TestNotificationConsumerSimple {
    private static final String CONSUMER_ENDPOINT = "simplisticConsumerEndpoint";

    public static void main(String[] args) throws Exception {
        System.out.println("This is a test to test a simple consumer.");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("To where should I send Subscribe?");

        String endpoint = bufferedReader.readLine();

        System.out.println("Trying to connect to: " + endpoint);

        NotificationConsumer consumer = new NotificationConsumer();
        consumer.quickBuild(CONSUMER_ENDPOINT);

        consumer.addConsumerListener(new ConsumerListener() {
            @Override
            public void notify(NotificationEvent event) {
                System.out.println("Received an Notification!");
                System.out.println("Time received: " + Calendar.getInstance());
                System.out.println("Timestamped: " + event.getTimestamp());
                try {
                    System.out.println(event.getXML());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("The main thread will now sleep for 60 seconds before it terminates");
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            System.out.println("Main thread was interrupted");
        }

        System.out.println("Exiting");
        System.exit(0);
    }
}
