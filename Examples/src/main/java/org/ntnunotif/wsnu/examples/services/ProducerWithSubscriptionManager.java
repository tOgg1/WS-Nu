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
import org.ntnunotif.wsnu.services.implementations.notificationproducer.GenericNotificationProducer;
import org.ntnunotif.wsnu.services.implementations.subscriptionmanager.SimplePausableSubscriptionManager;

/**
 *
 */
public class ProducerWithSubscriptionManager {

    private GenericNotificationProducer producer;
    private SimplePausableSubscriptionManager manager;

    private SoapForwardingHub myHub;

    private final String producerEndpoint = "myProducer";
    private final String managerEndpoint = "myManager";

    /**
     * Everything that needs to be done to set the system up can be done in the constructor.
     */
    public ProducerWithSubscriptionManager() {
        // Instantiate the Web Services
        producer = new GenericNotificationProducer();

        // We are using a pausable subscription manager here. A regular one could be added in the same fashion
        manager = new SimplePausableSubscriptionManager();
        // This makes our manager update itself every 60 seconds, checking for expired subscriptions
        manager.setScheduleInterval(60);

        // Quick build the Web services, thus initializing the entire system
        myHub = producer.quickBuild(producerEndpoint);
        manager.quickBuild(managerEndpoint, myHub);


        // This is the crucial part, we have to add the manager to the producer. If not, the producer
        // will not be able to receive events when a subscription is changed at the manager
        producer.setSubscriptionManager(manager);

        // And we are done really. Obviously you can at this stage start creating filters and similar voodoo. But this will
        // not be covered here.
    }
}
