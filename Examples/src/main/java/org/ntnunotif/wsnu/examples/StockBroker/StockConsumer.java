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
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tormod on 29.04.14.
 */
public class StockConsumer implements ConsumerListener {

    private NotificationConsumer consumer;
    private HashMap<String, StockPanel> stocks;

    private JFrame frame;
    private GridLayout layout;
    private ScrollPane scrollPane;
    private JPanel mainPanel;

    private Image background;

    public StockConsumer() {
        stocks = new HashMap<>();
    }

    public void initInterface(){
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.setResizable(false);

        layout = new GridLayout(0, 4);
        layout.setHgap(5);
        layout.setVgap(5);

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
            }
        };

        mainPanel.setLayout(layout);

        scrollPane = new ScrollPane();
        scrollPane.add(mainPanel);

        frame.getContentPane().add(scrollPane);
        frame.pack();
        frame.setVisible(true);
    }

    public void initWebservice(){
        consumer = new NotificationConsumer();
        consumer.addConsumerListener(this);
        consumer.quickBuild("stockConsumer");
        consumer.forceEndpointReference("http://"+ServiceUtilities.getExternalIp() + ":8080/stockConsumer");
        consumer.sendSubscriptionRequest("http://151.236.216.174:8080/stockBroker");

        ServiceUtilities.InputManager inputManager = new ServiceUtilities.InputManager();
        try {
            inputManager.addMethodReroute("exit", "^exit", true, System.class.getDeclaredMethod("exit", Integer.TYPE), this, new ServiceUtilities.Tuple[]{new ServiceUtilities.Tuple(0, 0)});
        } catch (NoSuchMethodException e) {
            // Do nothing
        }
    }

    public void addStock(StockChanged stock){
        StockPanel panel = new StockPanel(stock);
        stocks.put(stock.getSymbol(), panel);
        mainPanel.add(panel);
    }

    public void updateStock(StockChanged stock) {
        stocks.get(stock.getSymbol()).updateStock(stock);
    }

    // Update stocks
    @Override
    public void notify(NotificationEvent event) {
        List<NotificationMessageHolderType.Message> messages = event.getMessage();

        for (NotificationMessageHolderType.Message message : messages) {
            Object potentialStock = message.getAny();

            if(potentialStock instanceof StockChanged) {
                StockChanged stock = (StockChanged)potentialStock;

                if(stocks.containsKey(stock.getSymbol())){
                    updateStock(stock);
                } else {
                    addStock(stock);
                }
            } else {
                Log.w("StockConsumer", "Received an object that was not a StockChanged");
                continue;
            }
        }
    }

    private static class StockPanel extends JPanel {

        public static final int WIDTH = 200;
        public static final int HEIGHT = 100;

        private StockChanged stock;
        private JLabel name, value, changed, date;

        public StockPanel(StockChanged stock) {
            this.setLayout(new GridLayout(4,1));

            name = new JLabel();
            value = new JLabel();
            changed = new JLabel();
            date = new JLabel();

            name.setFont(new Font("Verdana", Font.BOLD, 15));
            value.setFont(new Font("Verdana", Font.BOLD, 10));
            changed.setFont(new Font("Verdana", Font.BOLD, 10));
            date.setFont(new Font("Verdana", Font.BOLD, 12));

            name.setForeground(new Color(33, 33, 33));
            value.setForeground(new Color(33, 33, 33));

            this.add(name);
            this.add(value);
            this.add(changed);
            this.add(date);

            updateStock(stock);
        }

        public void updateStock(StockChanged stock){
            this.stock = stock;

            name.setText(stock.getSymbol() + ":" + stock.getName());
            value.setText(Float.toString(stock.getValue()));

            String posStr = stock.getChangeAbsolute() > 0 ? "+" : "-";

            changed.setText(Float.toString(stock.getChangeAbsolute()) + "(" + stock.getChangeRelative());

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

        // Register our StockChanged object
        XMLParser.registerReturnObjectPackageWithObjectFactory("org.ntnunotif.wsnu.examples.StockBroker.generated");

        // Initialize the consumer
        StockConsumer consumer = new StockConsumer();
        consumer.initInterface();
        //consumer.initWebService();
    }
}
