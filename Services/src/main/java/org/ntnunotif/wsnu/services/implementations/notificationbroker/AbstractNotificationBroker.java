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

package org.ntnunotif.wsnu.services.implementations.notificationbroker;


import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.services.eventhandling.PublisherChangedListener;
import org.ntnunotif.wsnu.services.general.HelperClasses;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.AbstractNotificationProducer;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.brw_2.NotificationBroker;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationManager;

import java.util.List;

/**
 * The abstract base class of all NotificationBrokers.
 *
 * Implements some basic functionality of the NotificationBroker.
 */
public abstract class AbstractNotificationBroker extends AbstractNotificationProducer implements NotificationBroker, PublisherChangedListener {

    /**
     * Variable indicating whether the broker should require a publisher to register with it before
     * being allowed to send notifications.
     */
    protected boolean demandRegistered;

    /**
     * Variable indicating whether the broker should cache messages. Setting this to true allows retrieval through
     * {@link org.oasis_open.docs.wsn.b_2.GetCurrentMessage}.
     */
    protected boolean cacheMessages;

    /**
     * Reference to the registration manager used by this broker.
     */
    protected PublisherRegistrationManager registrationManager;

    /**
     * Default constructor
     */
    protected AbstractNotificationBroker() {
        super();
    }

    /**
     * Constructor passing along a hub to its {@link org.ntnunotif.wsnu.services.general.WebService} super.
     * @param hub Any {@link org.ntnunotif.wsnu.base.internal.Hub}
     */
    protected AbstractNotificationBroker(Hub hub) {
        super(hub);
    }

    /**
     * @see {@link #demandRegistered}
     * @param demandRegistered True or false.
     */
    public void setDemandRegistered(boolean demandRegistered) {
        this.demandRegistered = demandRegistered;
    }

    /**
     * @see {@link #cacheMessages} True or false.
     */
    public void setCacheMessages(boolean cacheMessages) {
        this.cacheMessages = cacheMessages;
    }

    /**
     * Clears the registration manager variable.
     */
    public void clearRegistrationManager(){
        registrationManager = null;
    }

    /**
     * Sets the registration manager.
     * @param registrationManager A {@link org.oasis_open.docs.wsn.brw_2.PublisherRegistrationManager}.
     */
    public void setRegistrationManager(PublisherRegistrationManager registrationManager) {
        this.registrationManager = registrationManager;
    }

    /**
     * An inner class used for the publisher registrations. Contains all relevant information regarding
     * a registration of a publisher.
     */
    public static class PublisherHandle {
        public final HelperClasses.EndpointTerminationTuple endpointTerminationTuple;
        public final List<TopicExpressionType> registeredTopics;
        public final boolean demand;

        public PublisherHandle(HelperClasses.EndpointTerminationTuple endpointTerminationTuple, List<TopicExpressionType> registeredTopics, boolean demand) {
            this.endpointTerminationTuple = endpointTerminationTuple;
            this.registeredTopics = registeredTopics;
            this.demand = demand;
        }
    }

}
