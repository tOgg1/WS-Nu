package org.ntnunotif.wsnu.examples.misc;

import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;

/**
 * Created by tormod on 10.05.14.
 */
public class WebServiceWithContentManager {

    private NotificationConsumer consumer;
    private final String endpointReference = "http://example.org/myConsumer";
    private ServiceUtilities.ContentManager inclusiveManager, exclusiveManager;

    public WebServiceWithContentManager() {
        consumer = new NotificationConsumer();
    }

    /**
     * Lets add some content managers.
     */
    public void addContentManagers() {
        // Our first content manager is an inclusive one, meaning it accepts all requests that matches any filter.
        inclusiveManager = new ServiceUtilities.ContentManager(endpointReference);
        inclusiveManager.setInclusive();
        // Lets add a match for the word "money"
        // After this, our content manager will accept any request with the word money in it.
        inclusiveManager.addContains("money");

        // Lets say we want to match any string starting with "free", we can do this with a regex
        inclusiveManager.addRegex("^free(.*)");

        // Lets add a content manager that excludes phrases we don't like
        exclusiveManager = new ServiceUtilities.ContentManager(endpointReference);
        exclusiveManager.setExclusive();

        // Lets filter out any containment of the word porn
        exclusiveManager.addContains("porn");

        // Lets also filter out any request that contains more than 3 occurrences of the word sex
        exclusiveManager.addCountLimitation("sex", 3);

        // Finally, lets add a complicated regex that filters out any request url that ends with 6 or more numbers
        exclusiveManager.addRegex("(.*)[0-9]{6,}$");

        // Lets add our mangers to our Web Service
        consumer.addContentManager(inclusiveManager);
        consumer.addContentManager(exclusiveManager);

        // In total we now only accept requests that
        // 1. Contains the word money
        // 2. Starts with the word free
        // 3. Does not contain the word porn
        // 4. Does not contain 3 or more occurrences of the word porn
        // 5. Does not end with 6 or more numbers
    }

    /**
     * Lets run a quick test testing our contentmanagers
     */
    public void runTest(){
        final String testOne = endpointReference + "/freemoneyporn";
        final String testTwo = endpointReference + "/freemoney";
        final String testThree = endpointReference + "/freemoney588394";
        final String testFour = endpointReference + "/freemoneysexmoney3434money";
        final String testFive = endpointReference + "/freesexmoneysexmoney3434moneysexsex";
        final String testSix = endpointReference + "/moneyfree";

        boolean testOneRes = exclusiveManager.accepts(testOne) && inclusiveManager.accepts(testOne);
        boolean testTwoRes = exclusiveManager.accepts(testTwo) && inclusiveManager.accepts(testTwo);
        boolean testThreeRes = exclusiveManager.accepts(testThree) && inclusiveManager.accepts(testThree);
        boolean testFourRes = exclusiveManager.accepts(testFour) && inclusiveManager.accepts(testFour);
        boolean testFiveRes = exclusiveManager.accepts(testFive) && inclusiveManager.accepts(testFive);
        boolean testSixRes = exclusiveManager.accepts(testSix) && inclusiveManager.accepts(testSix);


        // Should return false, we dont want the word porn
        System.out.println(testOneRes);

        if(testOneRes){
            throw new RuntimeException();
        }

        // Should return true, it starts with both free and contains money
        System.out.println(testTwoRes);


        if(!testTwoRes){
            throw new RuntimeException();
        }

        // Should return false, ends with 6 or more numbers
        System.out.println(testThreeRes);

        if(testThreeRes){
            throw new RuntimeException();
        }

        // Should return true, starts with free and contains money. Only has one occurrence of the word sex
        System.out.println(testFourRes);

        if(!testFourRes){
            throw new RuntimeException();
        }

        // Should return false, same as above, but with three occurrences of the word sex
        System.out.println(testFiveRes);

        if(testFiveRes){
            throw new RuntimeException();
        }


        // Should return false, does not start with free
        System.out.println(testSixRes);

        if(!testSixRes){
            throw new RuntimeException();
        }


    }

    public static void main(String[] args) {
        WebServiceWithContentManager webService = new WebServiceWithContentManager();
        webService.addContentManagers();
        webService.runTest();
    }


}
