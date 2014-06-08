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

package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;

import org.oasis_open.docs.wsn.bw_2.PausableSubscriptionManager;

/**
 *
 */
public abstract class AbstractPausableSubscriptionManager extends AbstractSubscriptionManager implements PausableSubscriptionManager{

    /**
     * Abstract method checking if a subscriptionIsPaused.
     * @return True if and only if the subscription exists, and it is currently paused.
     */
    public abstract boolean subscriptionIsPaused(String subscriptionReference);
}
