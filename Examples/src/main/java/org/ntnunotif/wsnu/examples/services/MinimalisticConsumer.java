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
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;

/**
 * A minimalistic notification consumer
 */
public class MinimalisticConsumer implements ConsumerListener {

    private NotificationConsumer consumer;

    private final String consumerEndpoint = "myConsumer";

    private SoapForwardingHub myHub;

    /**
     * Everything that needs to be done to set the system up can be done in the constructor.
     */
    public MinimalisticConsumer() {
        // Instantiate the consumer
        consumer = new NotificationConsumer();

        // By calling the consumers quickbuild method, we are starting both the consumer and the rest of the
        // system, all in one.
        myHub = consumer.quickBuild(consumerEndpoint);

        // Our class implements the ConsumerListener interface, meaning we can listen
        // on the NotificationConsumer for notifications
        consumer.addConsumerListener(this);

        // This is strictly speaking everything that needs to be done. However, we have not yet subscribed to anything
        // so we are not very likely to get any messages. See the BasicConsumerUse for a slightly more advanced example
    }

    /**
     * The implemented method from the ConsumerListener interface
     */
    @Override
    public void notify(NotificationEvent event) {
        System.out.println("I got a notify!");
    }
}
