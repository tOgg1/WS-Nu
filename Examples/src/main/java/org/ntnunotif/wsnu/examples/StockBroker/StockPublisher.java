package org.ntnunotif.wsnu.examples.StockBroker;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.base.util.Utilities;
import org.ntnunotif.wsnu.examples.StockBroker.generated.StockChanged;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.xmlsoap.schemas.soap.envelope.Body;
import org.xmlsoap.schemas.soap.envelope.Envelope;
import org.xmlsoap.schemas.soap.envelope.Header;
import org.xmlsoap.schemas.soap.envelope.ObjectFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/** Stock publisher example code.
 * This is a publisher that is publishing changes in stock-indexes.
 * It is listening to changes at http://www.reuters.com/finance/markets/indices.
 * When a change has been found, it sends its result to a NotificationBroker at 151.236.10.120
 */
public class StockPublisher {

     /*
     The reference to the ApplicationServer, so we can send requests out in to the internet
     */
    static ApplicationServer server;

    /*
     * Start the applicationserver without a connected hub. We dont need to accept any request (note that this will
     * return 404 to any request being sent here).
     */
    static{
        try {
            server = ApplicationServer.getInstance();
            server.startNoHub();
        } catch (Exception e) {
            System.exit(1);
        }
    }

    /**
     * Helper method to check if two Stocks are equal
     * @param one
     * @param two
     * @return
     */
    public static boolean equals(StockChanged one, StockChanged two){
        return one.getName().equals(two.getName()) && one.getSymbol().equals(two.getSymbol()) &&
               one.getLastChange().equals(two.getLastChange()) && assertFloat(one.getChangeAbsolute(), two.getChangeAbsolute()) &&
               assertFloat(one.getChangeAbsolute(), two.getChangeAbsolute()) && assertFloat(one.getValue(), two.getValue());
    }

    /**
     * Helper method to assert equality between floats
     * @param one
     * @param two
     * @return
     */
    public static boolean assertFloat(float one, float two){
        float absEps = 1e-4f;
        float relEps = 1e-5f;
        return (Math.abs(one - two) < absEps) && (1 - (one/two) < relEps);
    }

    /**
     * Helper method to generate a soap envelope
     * @return
     */
    public static javax.xml.bind.JAXBElement<Envelope> generateEnvelope(Object content){
        ObjectFactory soapFactory = new ObjectFactory();
        Envelope envelope = new Envelope();
        Body body = new Body();
        Header header = new Header();
        body.getAny().add(content);
        envelope.setBody(body);
        envelope.setHeader(header);

        return soapFactory.createEnvelope(envelope);
    }

    /**
     * A helper-class to assist us with inputmanaging, in case we want to do any console-work.
     */
    private ServiceUtilities.InputManager inputManager;

    /**
     * The scheduler to schedule a regular fetch of data.
     */
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * A reference to the scheduled task, so we can cancel it if we want
     */
    private ScheduledFuture<?> task;

    /**
     * A hashmap storing our known stocks. This so we can detect a change (we dont want to send anything when there isnt a change)
     */
    private HashMap<String, StockChanged> storedStocks = new HashMap<String, StockChanged>();

    /**
     * Reference to our broker
     */
    private String brokerReference = "http://151.236.216.174:8080/myBroker";

    /**
     * The constructor, initializing the inputmanager and sending a PublisherRegi
     * @throws Exception
     */
    public StockPublisher() throws Exception {
        inputManager = new ServiceUtilities.InputManager();

        /*
         * Take notice to this method. It adds a reroute for an input "update" to the method "update" in this class.
         */
        inputManager.addMethodReroute("update", "update", false, this.getClass().getMethod("update", null), this);
        inputManager.start();
    }

    /**
     * Start the StockPublisher. This starts the scheduler.
     */
    public void start() {
        resgisterWithBroker("151.236.10.120:8080");
        task = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                StockPublisher.this.update();
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    /**
     * Stop the StockPublisher. This stops the scheduler.
     */
    public void stop() {
        if(task != null){
            task.cancel(false);
        }
    }

    public void resgisterWithBroker(String endpoint){
        org.oasis_open.docs.wsn.br_2.RegisterPublisher registerPublisher = new org.oasis_open.docs.wsn.br_2.RegisterPublisher();
        registerPublisher.setDemand(false);
        // Change to your ip
        registerPublisher.setPublisherReference(ServiceUtilities.buildW3CEndpointReference("37.200.14.9"));
        try {
            Date date = new Date();
            date.setTime(System.currentTimeMillis() + 86400*365);
            GregorianCalendar now = new GregorianCalendar();
            now.setTime(date);
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            registerPublisher.setInitialTerminationTime(calendar);

            JAXBElement<Envelope> envelope = generateEnvelope(registerPublisher);

            InputStream inputStream = Utilities.convertUnknownToInputStream(envelope);
            InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_MESSAGE_IS_INPUTSTREAM, inputStream);

            // Set address
            RequestInformation requestInformation = new RequestInformation();
            requestInformation.setEndpointReference(brokerReference);
            message.setRequestInformation(requestInformation);

            InternalMessage returnMessage = server.sendMessage(message);

            if((returnMessage.statusCode & STATUS_OK) == 0){
                Log.e("StockPublisher", "Something went wrong at the broker");
            }
            Log.d("StockPublisher", "Succesfully registered with broker");
            System.out.println(returnMessage.getMessage());
        } catch (DatatypeConfigurationException e) {
            System.exit(1);
        }
    }

    public void sendUpdatedStock(StockChanged stockChanged){
        Notify notify = ServiceUtilities.createNotify(stockChanged, brokerReference, ApplicationServer.getURI());

        JAXBElement<Envelope> envelope = generateEnvelope(notify);

        InputStream inputStream = Utilities.convertUnknownToInputStream(envelope);

        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE| STATUS_MESSAGE_IS_INPUTSTREAM, inputStream);

        // Set address
        RequestInformation requestInformation = new RequestInformation();
        requestInformation.setEndpointReference(brokerReference);
        message.setRequestInformation(requestInformation);

        InternalMessage returnMessage = server.sendMessage(message);

        if((returnMessage.statusCode & STATUS_OK) == 0){
            Log.e("StockPublisher", "Something went wrong at the broker..." + returnMessage.getMessage());
        }
    }

    /**
     * Does the actual update logic.
     */
    public void update(){
        Log.d("StockPublisher", "Updating");
        InternalMessage message = new InternalMessage(STATUS_OK, null);

        RequestInformation info = new RequestInformation();
        info.setEndpointReference("http://www.reuters.com/finance/markets/indices");
        message.setRequestInformation(info);

        InternalMessage response = server.sendMessage(message);
        Object responseMessage = response.getMessage();

        String webPage = (String) responseMessage;
        Log.d("StockPublisher", "Got response with length: " + webPage.length());

        ArrayList<StockChanged> stocks = getStocks(webPage);
        Log.d("StockPublisher", "Parsed to " + stocks.size() + " stocks");

        for (StockChanged stock : stocks) {
            String symbol = stock.getSymbol();
            if(!storedStocks.containsKey(symbol)){
                System.out.println("Registered new stock: " + symbol);
                storedStocks.put(symbol, stock);
                sendUpdatedStock(stock);
            } else {
                if(!equals(storedStocks.get(symbol), stock)){
                    sendUpdatedStock(stock);
                    storedStocks.put(symbol, stock);
                }
            }
        }
    }

    /**
     * Fetches all stocks
     */
    public ArrayList<StockChanged> getStocks(String webPage){
        ArrayList<StockChanged> stocks = new ArrayList<StockChanged>();

        String america = "All American Indices";
        String europe = "All European Indices";
        String asia = "Asia/Pacific Indices";
        String africa = "Africa/Middle East Indices";

        int indexOne = webPage.indexOf(america);
        int indexTwo = webPage.indexOf(europe);
        int indexThree = webPage.indexOf(asia);
        int indexFour = webPage.indexOf(africa);

        String americaData = webPage.substring(indexOne, indexTwo);
        String europeData = webPage.substring(indexTwo, indexThree);
        String asiaData = webPage.substring(indexThree, indexFour);
        String africaData = webPage.substring(indexFour);

        // Jump over headers
        americaData = americaData.substring(americaData.indexOf("</tbody>"));
        europeData = europeData.substring(americaData.indexOf("</tbody>"));
        asiaData = asiaData.substring(americaData.indexOf("</tbody>"));
        africaData = africaData.substring(americaData.indexOf("</tbody>"));

        int tagStartIndex;
        int tagEndIndex;

        String[] dataSets = new String[]{americaData, europeData, asiaData, africaData};

        for (int i = 0; i < dataSets.length; i++) {
            String dataSet = dataSets[i];

            while((tagStartIndex = dataSet.indexOf("<tr")) > 0) {
                try {
                    tagEndIndex = dataSet.indexOf("</tr>", tagStartIndex + 1) + 5;

                    String row = dataSet.substring(tagStartIndex, tagEndIndex);

                    // Remove junk
                    row = row.replaceAll("\n", "");
                    row = row.replaceAll("\t", "");
                    row = row.replaceAll("<td><a href=\"/do/latestArticleInChannel[?]channel=(.*)MktRpt\">View</a></td>", "");
                    row = row.replaceAll("<td></td>", "");

                    // If our table-row isn't one we can work with
                    if(row.split("<td").length != 7){
                        dataSet = dataSet.substring(tagEndIndex);
                        continue;
                    }

                    int tagStart = row.indexOf("<td>") + 4;
                    int tagEnd = row.indexOf("</td>");

                    String symbolCell = row.substring(tagStart, tagEnd);
                    String symbolTemp = symbolCell.substring(symbolCell.indexOf("<a href=\"index?"));
                    String symbol = symbolTemp.substring(symbolTemp.indexOf(">") + 1, symbolTemp.indexOf("</a>"));

                    row = row.substring(tagEnd + 5);

                    tagStart = row.indexOf("<td") + 4;
                    tagEnd = row.indexOf("</td>");

                    String nameCell = row.substring(tagStart, tagEnd);
                    String name = nameCell;

                    row = row.substring(tagEnd + 5);

                    tagStart = row.indexOf("<td");
                    tagEnd = row.indexOf("</td>");

                    String dateCell = row.substring(tagStart, tagEnd);
                    dateCell = dateCell.replaceAll("<!--(.*)-->", "");
                    String date = dateCell.substring(dateCell.indexOf(">") + 1);

                    row = row.substring(tagEnd + 5);

                    tagStart = row.indexOf("<td");
                    tagEnd = row.indexOf("</td>");

                    String valueCell = row.substring(tagStart, tagEnd);
                    String value = valueCell.substring(valueCell.indexOf(">") + 1);

                    float valueParsed = Float.parseFloat(value.replaceAll(",", ""));

                    row = row.substring(tagEnd + 5);

                    tagStart = row.indexOf("<td");
                    tagEnd = row.indexOf("</td>");

                    String changeAbsCell = row.substring(tagStart, tagEnd);
                    String changeAbs = changeAbsCell.substring(changeAbsCell.indexOf(">") + 1);

                    float changeAbsParsed = Float.parseFloat(changeAbs.replaceAll("[+]?[-]?[,]?", ""));
                    changeAbsParsed = changeAbs.charAt(0) == '+' ? changeAbsParsed : -changeAbsParsed;

                    row = row.substring(tagEnd + 5);

                    tagStart = row.indexOf("<td");
                    tagEnd = row.indexOf("</td>");

                    String changeRelCell = row.substring(tagStart, tagEnd);
                    String changeRel = changeRelCell.substring(changeRelCell.indexOf(">") + 1);

                    float changeRelParsed = Float.parseFloat(changeRel.replaceAll("[+]?[-]?[,]?", "").replaceAll("%", ""));
                    changeRelParsed = changeRel.charAt(0) == '+' ? changeRelParsed : -changeRelParsed;

                    org.ntnunotif.wsnu.examples.StockBroker.generated.StockChanged stockChanged = new StockChanged();
                    stockChanged.setSymbol(symbol);
                    stockChanged.setName(name);
                    stockChanged.setLastChange(date);
                    stockChanged.setValue(valueParsed);
                    stockChanged.setChangeAbsolute(changeAbsParsed);
                    stockChanged.setChangeRelative(changeRelParsed);

                    stocks.add(stockChanged);
                    dataSet = dataSet.substring(tagEndIndex);

                // If something goes wrong, we just continue, and try some more
                } catch(Exception e) {
                    continue;
                }
            }
        }
        return stocks;
    }

    public static void main(String[] args) {
        XMLParser.registerReturnObjectPackageWithObjectFactory("org.ntnunotif.wsnu.examples.StockBroker.generated");
        StockPublisher publisher = null;
        try {
            publisher = new StockPublisher();
            publisher.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
