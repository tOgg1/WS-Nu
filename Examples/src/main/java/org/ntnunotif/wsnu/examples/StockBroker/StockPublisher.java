//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

package org.ntnunotif.wsnu.examples.StockBroker;

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.ntnunotif.wsnu.base.util.Utilities;
import org.ntnunotif.wsnu.examples.StockBroker.generated.StockChanged;
import org.ntnunotif.wsnu.services.general.HelperClasses;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.general.WsnUtilities;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Stock publisher example code.
 *
 * This is a publisher that is publishing changes in stock-indexes.
 * It is listening to changes at http://www.reuters.com/finance/markets/indices.
 * When a change has been found, it sends its result to a NotificationBroker.
 */
public class StockPublisher {

    // The reference to the ApplicationServer, so we can send requests out in to the internet
    static ApplicationServer server;

     // Start the applicationserver without a connected hub. We don't need to accept any request (note that this will
     // return 404 to any request being sent here).
    static{
        try {
            // Get the ApplicationServer-singleton
            server = ApplicationServer.getInstance();

            // This is a method that starts the server without a hub.
            server.startNoHub();
        } catch (Exception e) {
            System.exit(1);
        }
    }


    // Helper method to check if two Stocks are equal
    public static boolean equals(StockChanged one, StockChanged two){
        return one.getName().equals(two.getName()) && one.getSymbol().equals(two.getSymbol()) &&
               one.getLastChange().equals(two.getLastChange()) && assertFloat(one.getChangeAbsolute(), two.getChangeAbsolute()) &&
               assertFloat(one.getChangeAbsolute(), two.getChangeAbsolute()) && assertFloat(one.getValue(), two.getValue());
    }

    // Helper method to assert equality between floats
    public static boolean assertFloat(float one, float two){
        float absEps = 1e-4f;
        float relEps = 1e-5f;
        return (Math.abs(one - two) < absEps) && (1 - (one/two) < relEps);
    }

    // Helper method to generate a soap envelope
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


    // A helper-class to assist us with inputmanaging, in case we want to do any console-work.
    private HelperClasses.InputManager inputManager;

    // The scheduler to schedule a regular fetch of data.
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    // A reference to the scheduled task, so we can cancel it if we want
    private ScheduledFuture<?> task;


    // A hashmap storing our known stocks. This so we can detect a change (we dont want to send anything when there isnt a change)
    private HashMap<String, StockChanged> storedStocks = new HashMap<>();

    // Reference to our broker
    private String brokerReference = "http://127.0.0.1:8080/myBroker";

    public StockPublisher() {
        // Initialize the inputManager
        try{
            inputManager = new HelperClasses.InputManager();
            inputManager.addMethodReroute("update", "update", false, this.getClass().getMethod("update", null), this);
            inputManager.start();
        }catch(NoSuchMethodException e){
            // Do nothing
        }
    }

    // Start the scheduler.
    public void start() {
        resgisterWithBroker("127.0.0.1:8080");
        task = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                StockPublisher.this.update();
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    // Stop the scheduler.
    public void stop() {
        if(task != null){
            task.cancel(false);
        }
    }

    // This method registers this publisher with the NotificationBroker
    public void resgisterWithBroker(String endpoint) {

        // Create the actual registerPublisher object
        org.oasis_open.docs.wsn.br_2.RegisterPublisher registerPublisher = new org.oasis_open.docs.wsn.br_2.RegisterPublisher();

        // We don't want to enforce demand-based publishing
        registerPublisher.setDemand(false);

        // The reference of this publisher. This needs to be changed to your relevant IP-address.
        registerPublisher.setPublisherReference(ServiceUtilities.buildW3CEndpointReference("127.0.0.1:8080"));
        try {
            // Create relevant information

            // Set the expiration date one year from now... just in case.
            Date date = new Date();
            date.setTime(System.currentTimeMillis() + 86400*365);
            GregorianCalendar now = new GregorianCalendar();
            now.setTime(date);
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            registerPublisher.setInitialTerminationTime(calendar);

            // Create a Soap envelope and add our registerPublisher to it. Note that functionality like this will
            // be moved to ServiceUtilities with the release of 0.4
            JAXBElement<Envelope> envelope = generateEnvelope(registerPublisher);

            // Create an InputStream
            InputStream inputStream = Utilities.convertUnknownToInputStream(envelope);
            InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE|STATUS_MESSAGE_IS_INPUTSTREAM, inputStream);

            // Set the broker address. How this actually works is specified in the technical specification. But a quick recap:
            // The ApplicationServer peeks into the InternalMessage it receives and looks for the RequestInformations endpoint-
            // reference, and then sends
            RequestInformation requestInformation = new RequestInformation();
            requestInformation.setEndpointReference(brokerReference);
            message.setRequestInformation(requestInformation);

            // Send the message through the server. We here don't operate with a hub, so we send it directly to the server.
            InternalMessage returnMessage = server.sendMessage(message);

            // If the returnMessage's state is not flagged as OK, something went wrong at the broker.
            if((returnMessage.statusCode & STATUS_OK) == 0) {
                Log.e("StockPublisher", "Something went wrong at the broker");
            }
            Log.d("StockPublisher", "Succesfully registered with broker");
            // Print the return message for debugging purposes
            System.out.println(returnMessage.getMessage());
        } catch (DatatypeConfigurationException e) {
            System.exit(1);
        }
    }

    // Publish and updatedStock
    public void sendUpdatedStock(StockChanged stockChanged){
        // Create the actual notify. Note that the third argument is the producerreference, which we in this scenario set to
        // the ip of the applicationserver
        Notify notify = WsnUtilities.createNotify(stockChanged, brokerReference, ApplicationServer.getURI());

        // Create the soap envelope
        JAXBElement<Envelope> envelope = generateEnvelope(notify);

        // Convert the envelope to an inputstream
        InputStream inputStream = Utilities.convertUnknownToInputStream(envelope);

        // Create the internalmessages
        InternalMessage message = new InternalMessage(STATUS_OK|STATUS_HAS_MESSAGE| STATUS_MESSAGE_IS_INPUTSTREAM, inputStream);

        // Set address. See previous method for more information
        RequestInformation requestInformation = new RequestInformation();
        requestInformation.setEndpointReference(brokerReference);
        message.setRequestInformation(requestInformation);

        // Send the message through the server. We here don't operate with a hub, so we send it directly to the server.
        InternalMessage returnMessage = server.sendMessage(message);

        // If the returnMessage's state is not flagged as OK, something went wrong at the broker.
        if((returnMessage.statusCode & STATUS_OK) == 0){
            Log.e("StockPublisher", "Something went wrong at the broker..." + returnMessage.getMessage());
        }
    }

    // Does the actual update logic.
    public void update(){
        Log.d("StockPublisher", "Updating");

        // Send the request to the reuters.com page
        InternalMessage message = new InternalMessage(STATUS_OK, null);

        RequestInformation info = new RequestInformation();
        info.setEndpointReference("http://www.reuters.com/finance/markets/indices");
        message.setRequestInformation(info);

        // Send the message
        InternalMessage response = server.sendMessage(message);
        Object responseMessage = response.getMessage();

        // Our webpage comes back as a string
        String webPage = (String) responseMessage;
        Log.d("StockPublisher", "Got response with length: " + webPage.length());

        // Parse the webpage
        ArrayList<StockChanged> stocks = getStocks(webPage);
        Log.d("StockPublisher", "Parsed to " + stocks.size() + " stocks");

        // Loop over the stocks and figure out if we have new stocks, or updated stocks
        for (StockChanged stock : stocks) {
            String symbol = stock.getSymbol();
            // We have a new stock
            if(!storedStocks.containsKey(symbol)){
                System.out.println("Registered new stock: " + symbol);
                storedStocks.put(symbol, stock);
                sendUpdatedStock(stock);
            // We already have the stock
            } else {
                // If our stock has updated
                if(!equals(storedStocks.get(symbol), stock)){
                    sendUpdatedStock(stock);
                    storedStocks.put(symbol, stock);
                }
            }
        }
    }

    // Parses the stocks. The below will not be documented. It is essentially a sequential search parser finding the relevant
    // information from the reuters.com webpage
    public ArrayList<StockChanged> getStocks(String webPage){
        ArrayList<StockChanged> stocks = new ArrayList<>();

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
        // Do some initialization.
        //
        // Make sure we are logging everything, and writing to log file.
        Log.initLogFile();
        Log.setEnableDebug(true);
        Log.setEnableWarnings(true);
        Log.setEnableErrors(true);

        // Register our StockChanged object. This is paramount if we want our Parser to be able to handle the StockChanged
        // objects.
        XMLParser.registerReturnObjectPackageWithObjectFactory("org.ntnunotif.wsnu.examples.StockBroker.generated");

        // Start our publisher
        StockPublisher publisher = null;
        publisher = new StockPublisher();
        publisher.start();
    }
}
