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

package org.ntnunotif.wsnu.base.internal;

import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.RequestInformation;

/**
 * Created by tormod on 25.03.14.
 */
public interface ServiceConnection {

    /**
     * Accept a message from a hub. This function forwards the message to its connected Web Service.
     *
     * @param message
     * @return Returns the appropriate response, null if no response is expected, or the message could not be processed
     */
    public InternalMessage acceptMessage(InternalMessage message);

    /**
     * Accepts <b>only</b> an url-request. This message does not have any content.
     *
     * @param message
     * @return
     */
    public InternalMessage acceptRequest(InternalMessage message);

    /**
     * Return the type of the Web Service connected to this connection.
     *
     * @return The type as a class
     */
    public Class getServiceType();

     /**
      * Get the endpoint reference of this service.
     */
    public String getServiceEndpoint();

    /**
     * Fetches the request-information currently set in the connector.
     * @return
     */
    public RequestInformation getRequestInformation();


    /**
     * Function called when the endpointReference of the connected web service is changed.
     * Whether or not the implementing connector decides to react or not is implementation-specific.
     * @param newEndpointReference
     */
    public void endpointUpdated(String newEndpointReference);
}

