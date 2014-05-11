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

package org.ntnunotif.wsnu.services.eventhandling;

/**
 * A listener meant to be implemented by implementing classes of
 * {@link org.ntnunotif.wsnu.services.implementations.notificationbroker.AbstractNotificationBroker} or
 * {@link org.ntnunotif.wsnu.services.implementations.notificationproducer.AbstractNotificationProducer}.
 *
 * Has one method, {@link #subscriptionChanged(SubscriptionEvent)}, which sends a
 * {@link org.ntnunotif.wsnu.services.eventhandling.SubscriptionEvent} object.
 *
 * The main intention behind having this class is making it easy for Producers and Brokers to be notified when a subscription
 * has changed in any form.
 */
public interface SubscriptionChangedListener {

    public void subscriptionChanged(SubscriptionEvent event);
}
