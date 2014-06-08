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
import org.ntnunotif.wsnu.services.implementations.notificationbroker.NotificationBrokerImpl;

/**
 * Example showing very basic use of the NotificationBroker. This example will not go detailed into how
 * filters etc. are created. This information can be found in {@link org.ntnunotif.wsnu.examples.services.BasicProducerUse}.
 */
public class MinimalisticBroker {

    private NotificationBrokerImpl broker;

    private final String brokerEndpoint = "myBroker";

    private SoapForwardingHub myHub;
    /**
     * We can do everything needed in the constructor
     */
    public MinimalisticBroker() {
        // Instantiate the broker
        broker = new NotificationBrokerImpl();

        // By calling the brokers quickbuild method, we are starting both the broker and the rest of the
        // system, all in one.
        myHub = broker.quickBuild(brokerEndpoint);

        // This method makes our Broker not demand that a publisher publishing on the broker
        // is registered with the broker beforehand
        broker.setDemandRegistered(false);

        // This method makes our broker cache messages, for use with the getCurrentMessage web method.
        broker.setCacheMessages(true);

        // This is the most minimalistic example of creating a NotificationBroker possible. For more information
        // on filtersupport etc. see the BasicProducerUse example
    }
}
