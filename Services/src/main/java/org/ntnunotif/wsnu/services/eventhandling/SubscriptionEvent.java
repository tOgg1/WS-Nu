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
 * The class contains information regarding a change in a subscription.
 *
 * Contained in an object of this class is a registration reference, as well as an enum-variable indicating
 * what type of change has occured.
 */
public final class SubscriptionEvent {

    private final String subscriptionReference;
    private final Type type;

    /**
     * Main and only constructor.
     * @param subscriptionReference Reference to the subscription-key.
     * @param type The type of change.
     */
    public SubscriptionEvent(String subscriptionReference, Type type) {
        this.subscriptionReference = subscriptionReference;
        this.type = type;
    }

    /**
     * @return The subscription reference.
     */
    public String getSubscriptionReference() {
        return subscriptionReference;
    }

    /**
     * @return The type of change.
     */
    public Type getType() {
        return type;
    }

    /**
     * Type-enum, containing all possible change types of a subscription.
     */
    public static enum Type{
        UNSUBSCRIBE,
        RENEW,
        PAUSE,
        RESUME
    }
}
