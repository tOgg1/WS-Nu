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
 * {@link org.ntnunotif.wsnu.services.implementations.notificationbroker.AbstractNotificationBroker}.
 *
 * Has one method, {@link #publisherChanged(PublisherRegistrationEvent)}, which sends a
 * {@link org.ntnunotif.wsnu.services.eventhandling.PublisherRegistrationEvent} object.
 *
 * The main intention behind having this class is making it easy for Brokers to be notified when a publisher registration
 * has changed in any form. (Per the 2006 WS-N specification the only change can be destruction).
 */
public interface PublisherChangedListener {

    /**
     * The method indicating that a publisher registration has changed.
     * @param event The event-object containing information regarding the actual change.
     */
    public void publisherChanged(PublisherRegistrationEvent event);
}
