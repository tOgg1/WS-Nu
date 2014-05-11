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

package org.ntnunotif.wsnu.examples.services;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.services.implementations.notificationbroker.GenericNotificationBroker;
import org.ntnunotif.wsnu.services.implementations.publisherregistrationmanager.SimplePublisherRegistrationManager;
import org.ntnunotif.wsnu.services.implementations.subscriptionmanager.SimpleSubscriptionManager;

/**
 * Example of a NotificationBroker with a SubscriptionManager and a PublisherRegistrationManager
 */
public class BrokerWithSubscriptionManagerAndPublisherRegistrationManager {

    private GenericNotificationBroker broker;
    private SimpleSubscriptionManager subscriptionManager;
    private SimplePublisherRegistrationManager publisherRegistrationManager;

    private final String brokerEndpoint = "myBroker";
    private final String subscriptionManagerEndpoint = "mySubscriptionManager";
    private final String publisherRegistrationManagerEndpoint = "publisherRegistrationManagerEndpoint";

    private SoapForwardingHub myHub;

    /**
     * We can do everything needed in the constructor
     */
    public BrokerWithSubscriptionManagerAndPublisherRegistrationManager() {

        // Instantiate the Web Services
        broker = new GenericNotificationBroker();
        subscriptionManager = new SimpleSubscriptionManager();
        publisherRegistrationManager = new SimplePublisherRegistrationManager();

        // Quick build the Web services, thus initializing the entire system
        myHub = broker.quickBuild(brokerEndpoint);
        subscriptionManager.quickBuild(subscriptionManagerEndpoint, myHub);
        publisherRegistrationManager.quickBuild(publisherRegistrationManagerEndpoint, myHub);

        // This is the crucial part, we need to add both the subscription manager and the publisher registration manager
        // to the broker. This is necessary to allow the Broker to react to changes in the subscriptions/registrations occurring
        // at the subscription manager and publisher registration manager
        broker.setSubscriptionManager(subscriptionManager);
        broker.setRegistrationManager(publisherRegistrationManager);
    }
}
