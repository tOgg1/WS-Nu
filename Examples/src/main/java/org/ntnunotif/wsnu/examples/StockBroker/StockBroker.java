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

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.general.HelperClasses;
import org.ntnunotif.wsnu.services.implementations.notificationbroker.NotificationBrokerImpl;
import org.ntnunotif.wsnu.services.implementations.publisherregistrationmanager.SimplePublisherRegistrationManager;

/**
 * A StockBroker (pun intended). This class implements the
 * {@link org.ntnunotif.wsnu.services.implementations.notificationbroker.NotificationBrokerImpl} interface
 * with the following settings:
 *
 * <ul>
 *     <li>Demand registered = True</li>
 *     <li>Cache messages = True</li>
 * </ul>
 *
 * Also attaches a {@link org.ntnunotif.wsnu.services.implementations.publisherregistrationmanager.SimplePublisherRegistrationManager}
 * to the broker.
 *
 * Created by tormod on 29.04.14.
 */
public class StockBroker {

    // Our NotificiationBroker Web Service
    private NotificationBrokerImpl broker;

    // Our PublisherRegistrationManager Web Service
    private SimplePublisherRegistrationManager manager;

    // Our hub. In this example we need to keep a reference of the hub to be able to add more than one web service to it
    private Hub hub;

    public StockBroker() {

    }

    public void start() throws Exception {

        // Start and initialize our NotificationBrokerImpl. We want a Broker that
        // demands the publisher to be registered beforehand, and we also want it to cache messages.
        // Confuse not the "setDemandRegistered"-method with demand-based publishing.
        broker = new NotificationBrokerImpl();
        broker.setCacheMessages(true);
        broker.setDemandRegistered(true);
        hub = broker.quickBuild("myBroker");

        // Generate WSDL and XSD-schemas for the Web Service. Note that this can – and should – be done manually by
        // the deployer (you), as you will have more control over output.
        broker.generateWSDLandXSDSchemas();

        // Create a SimplePublisherRegistrationManager to attach to our broker. This needs no configuration: it works
        // "out of the box".
        manager = new SimplePublisherRegistrationManager();

        // This a method added in version 0.2. It quickBuilds in the same fashion as the method that doesn't take a hub
        // as an argument. It differs by not actually building a new hub, and not initializing the Application Server.
        manager.quickBuild("registrationManager", hub);

        // Sets the brokers RegistrationManager to our manager. This explicit adding is essential, as without it,
        // the destroyRegistration Web Method will not propagate its results to the broker. (This can lead to the broker
        // never actually removing its subscriptions.
        broker.setRegistrationManager(manager);

        // Creates an InputManager. Please see the JavaDocs for more information.
        HelperClasses.InputManager inputManager = new HelperClasses.InputManager();
        inputManager.addMethodReroute("exit", "^exit", true, System.class.getDeclaredMethod("exit", Integer.TYPE), this, new HelperClasses.Tuple[]{new HelperClasses.Tuple(0, 0)});
        inputManager.start();
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
        StockBroker broker = new StockBroker();
        try {
            broker.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
