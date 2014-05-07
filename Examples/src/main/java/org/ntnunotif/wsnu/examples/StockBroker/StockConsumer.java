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
 * Created by tormod on 29.04.14.
 */
public class StockConsumer implements ConsumerListener {

    private final NotificationConsumer consumer;
    private HashMap<String, StockPanel> stocks;

    private JFrame frame;
    private GridBagLayout layout;
    private GridBagConstraints gb;
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JPanel backgroundPanel;

    private Image background;

    public StockConsumer() {
        stocks = new HashMap<>();
        consumer = new NotificationConsumer();
    }

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

    public void initWebservice(){
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

        gb.gridx = (stocks.size()-1) % 3;
        gb.gridy = (int) Math.floor((stocks.size()-1) / 3);

        System.out.println("x:" + gb.gridx);
        System.out.println("y:" + gb.gridy);

        mainPanel.add(panel, gb);
        mainPanel.repaint();
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

        // Register our StockChanged object
        XMLParser.registerReturnObjectPackageWithObjectFactory("org.ntnunotif.wsnu.examples.StockBroker.generated");

        // Initialize the consumer
        final StockConsumer consumer = new StockConsumer();
        consumer.initInterface();
        consumer.initWebservice();
    }
}
