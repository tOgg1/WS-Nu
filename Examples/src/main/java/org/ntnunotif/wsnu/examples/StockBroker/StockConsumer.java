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

import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.examples.StockBroker.generated.StockChanged;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

/**
 * Example consumer. The StockConsumer implements a NotificationConsumer interface to receive
 * {@link org.ntnunotif.wsnu.examples.StockBroker.generated.StockChanged} objects.
 *
 * Uses the {@link javax.swing} library to implement a graphical user interface.
 *
 * Created by tormod on 29.04.14.
 */
public class StockConsumer implements ConsumerListener {


    // The actual NotificationConsumer interface. Note that we have declared it final to avoid
    // having it removed from context and cleaned up from the garbage collector.
    private final NotificationConsumer consumer;

    // A HashMap of our stocks.
    private HashMap<String, StockPanel> stocks;

    // Gui-Objects
    private JFrame frame;
    private GridBagLayout layout;
    private GridBagConstraints gb;
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JPanel backgroundPanel;
    private Image background;

    // Initializing relevant objects
    public StockConsumer() {
        stocks = new HashMap<>();
        consumer = new NotificationConsumer();
    }

    // Initialize interface. For more information, see http://docs.oracle.com/javase/tutorial/uiswing/
    public void initInterface(){
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.setResizable(false);

        layout = new GridBagLayout();

        gb = new GridBagConstraints();
        gb.anchor = GridBagConstraints.CENTER;
        gb.insets = new Insets(10, 10, 10, 10);

        try {
            background = ImageIO.read(getClass().getResourceAsStream("/background_consumer.png"));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't find background-image! This is is completely unacceptable.");
        }

        mainPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(StockConsumer.this.background, 0, 0, null);

                for (int i = 600; i < mainPanel.getHeight(); i+=600) {
                    g.drawImage(StockConsumer.this.background, 0, i, null);
                }
            }
        };

        EmptyBorder border = new EmptyBorder(15, 15, 15, 15);

        mainPanel.setLayout(layout);

        mainPanel.setBackground(new Color(0,0,0,0));
        mainPanel.setBorder(border);

        scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //scrollPane.setBackground(new Color(0,0,0,0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        //backgroundPanel.add(scrollPane);

        frame.getContentPane().add(scrollPane);
        frame.pack();
        frame.setVisible(true);
    }

    // Initialize the Web Service
    public void initWebservice(){
        // This class is a ConsumerListener, meaning any notification received by our NotificationConsumer-interface
        // will be forwarded here. This given we register "this" as a consumerlistener with the NotificationConsumer.
        consumer.addConsumerListener(this);

        // This method builds the Hub, ApplicationServer and a Connector. It then adds the NotificationConsumer to the consumer
        // and registers the service with the hub. TL;DR: This method does everything for you.
        consumer.quickBuild("stockConsumer");

        // This is a "hack", allowing us to set the endpoint-reference to our external-ip. This should ideally be set in the
        // ApplicationServer config. But this would be hard to generalize for an example-code.
        consumer.forceEndpointReference("http://"+ServiceUtilities.getExternalIp() + ":8080/stockConsumer");

        // We send a subscription-request to our broker. This ip needs to be changed to the IP of your broker.
        consumer.sendSubscriptionRequest("http://151.236.216.174:8080/stockBroker");

        // Sets up an inputmanager, see the JavaDocs for how to use this.
        ServiceUtilities.InputManager inputManager = new ServiceUtilities.InputManager();
        try {
            inputManager.addMethodReroute("exit", "^exit", true, System.class.getDeclaredMethod("exit", Integer.TYPE), this, new ServiceUtilities.Tuple[]{new ServiceUtilities.Tuple(0, 0)});
        } catch (NoSuchMethodException e) {
            // Do nothing
        }
    }

    // Adds a stock to the GUI.
    public void addStock(StockChanged stock){
        StockPanel panel = new StockPanel(stock);
        stocks.put(stock.getSymbol(), panel);

        gb.gridx = (stocks.size()-1) % 3;
        gb.gridy = (int) Math.floor((stocks.size()-1) / 3);

        System.out.println("x:" + gb.gridx);
        System.out.println("y:" + gb.gridy);

        mainPanel.add(panel, gb);
        mainPanel.repaint();
    }

    // Update our StockPanel
    public void updateStock(StockChanged stock) {
        stocks.get(stock.getSymbol()).updateStock(stock);
    }

    // This method is the implemented method from ConsumerListener. Whenever a NotificationConsumer receives a
    // Notify, it sends this event to every ConsumerListener listening.
    @Override
    public void notify(NotificationEvent event) {
        // Get all the NotificationMessage objects contained in the Notify. In this example the count of NotificationMessages
        // will always be 1.
        List<NotificationMessageHolderType.Message> messages = event.getMessage();

        // Loop over all NotificationMessages
        for (NotificationMessageHolderType.Message message : messages) {

            // Fetch the message content
            Object potentialStock = message.getAny();

            // If the contained message actually is a potentialStock.
            if(potentialStock instanceof StockChanged) {
                StockChanged stock = (StockChanged)potentialStock;

                // Have we already got the stock? If yes, send and update ...
                if(stocks.containsKey(stock.getSymbol())){
                    updateStock(stock);
                // .. if not, add a new stock
                } else {
                    addStock(stock);
                }

            // We don't want to handle anything else than a StockChanged
            } else {
                Log.w("StockConsumer", "Received an object that was not a StockChanged");
                continue;
            }
        }
    }

    // The StockPanel. As before, see the swing documentation for specifics.
    private static class StockPanel extends JPanel {

        public static final int WIDTH = 230;
        public static final int HEIGHT = 95;

        private StockChanged stock;
        private JLabel symbol, name, value, changed, date;

        private static DecimalFormat format = new DecimalFormat("##.##");
        private static GridBagConstraints gb  = new GridBagConstraints();

        public StockPanel(StockChanged stock) {
            this.setLayout(new GridBagLayout());
            this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            this.setSize(WIDTH, HEIGHT);
            this.setBackground(new Color(46, 46, 46, 200));

            symbol = new JLabel();
            name = new JLabel();
            value = new JLabel();
            changed = new JLabel();
            date = new JLabel();

            symbol.setFont(new Font("Sans", Font.BOLD, 11));
            name.setFont(new Font("Sans", Font.BOLD, 12));
            value.setFont(new Font("Sans", Font.BOLD, 10));
            changed.setFont(new Font("Sans", Font.BOLD, 10));
            date.setFont(new Font("Sans", Font.BOLD, 11));

            symbol.setForeground(new Color(208, 208, 208));
            name.setForeground(new Color(208, 208, 208));
            value.setForeground(new Color(208, 208, 208));
            date.setForeground(new Color(208, 208, 208));

            gb.gridx = 1;
            gb.gridy = 1;
            gb.gridwidth = 1;
            gb.insets = new Insets(0,3, 0, 3);
            gb.anchor = GridBagConstraints.CENTER;

            //this.add(symbol, gb);
            gb.gridx = 1;
            this.add(name, gb);
            gb.gridy = 2;
            gb.gridx = 1;
            this.add(value, gb);
            gb.gridy = 3;
            this.add(changed, gb);
            gb.gridy = 4;
            this.add(date, gb);

            updateStock(stock);
        }

        // Update the stock
        public void updateStock(StockChanged stock){
            this.stock = stock;

            //symbol.setText("Symbol: " + stock.getSymbol());
            name.setText(stock.getName() + " (" + stock.getSymbol()+")");

            boolean isPositive = stock.getChangeAbsolute() > 0 ? true : false;
            String posStr = stock.getChangeAbsolute() > 0 ? "+" : "";

            String changedText = "Change: " + posStr + format.format(stock.getChangeAbsolute()) + "(" + posStr + format.format(stock.getChangeRelative()) + "%)";

            Color changedColor;
            if(isPositive) {
                changedColor = new Color(39, 141, 39);
            } else {
                changedColor = new Color(141, 39, 39);
            }

            value.setText("Value: " + format.format(stock.getValue()));
            value.setForeground(changedColor);

            changed.setText(changedText);
            changed.setForeground(changedColor);

            date.setText("Last changed: " + stock.getLastChange());

            this.repaint();
        }
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

        // Initialize the consumer
        StockConsumer consumer = new StockConsumer();
        consumer.initInterface();
        consumer.initWebservice();
    }
}
