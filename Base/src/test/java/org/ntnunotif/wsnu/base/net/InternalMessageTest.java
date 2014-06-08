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

package org.ntnunotif.wsnu.base.net;

import junit.framework.TestCase;
import org.junit.Test;
import org.ntnunotif.wsnu.base.util.InternalMessage;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 *
 */
public class InternalMessageTest extends TestCase {

    @Test
    public void testStatusCodes(){
        /* Test single codes */
        InternalMessage messageOk = new InternalMessage(STATUS_OK, null);
        InternalMessage messageFault = new InternalMessage(STATUS_FAULT, null);
        InternalMessage messageWrongEndpoint = new InternalMessage(STATUS_FAULT_INVALID_DESTINATION, null);

        assertTrue((STATUS_OK & messageOk.statusCode) > 0);
        assertTrue((STATUS_FAULT & messageFault.statusCode) > 0);
        assertTrue((STATUS_FAULT_INVALID_DESTINATION & messageWrongEndpoint.statusCode) > 0);

        assertTrue((STATUS_FAULT & messageOk.statusCode) == 0);

        /* Test various fault-flags */
        InternalMessage messageInternalFault = new InternalMessage(STATUS_FAULT|STATUS_FAULT_INTERNAL_ERROR, null);
        InternalMessage messageUnknownFault = new InternalMessage(STATUS_FAULT|STATUS_FAULT_UNKNOWN_METHOD, null);

        assertTrue((STATUS_FAULT & messageInternalFault.statusCode) > 0);
        assertTrue((STATUS_FAULT_INVALID_PAYLOAD & messageInternalFault.statusCode) == 0);
        assertTrue((STATUS_FAULT_UNKNOWN_METHOD & messageUnknownFault.statusCode) > 0);

        /* Test multiple codes */
        InternalMessage messageInternalFaultUnknownDestination = new InternalMessage(STATUS_FAULT|STATUS_FAULT_INTERNAL_ERROR| STATUS_FAULT_INVALID_DESTINATION, null);

        assertTrue((STATUS_FAULT & messageInternalFaultUnknownDestination.statusCode) > 0);
        assertTrue((STATUS_FAULT_INTERNAL_ERROR & messageInternalFaultUnknownDestination.statusCode) > 0);
        assertTrue((STATUS_FAULT_INVALID_DESTINATION & messageInternalFaultUnknownDestination.statusCode) > 0);
        assertTrue(((STATUS_FAULT | STATUS_FAULT_INTERNAL_ERROR) & messageInternalFaultUnknownDestination.statusCode) > 0);
    }
}
