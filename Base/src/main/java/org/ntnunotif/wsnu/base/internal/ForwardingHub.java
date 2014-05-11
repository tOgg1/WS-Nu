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

import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.InternalMessage;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Hub that forwards any message it gets directly to its connected WebServices
 * Created by tormod on 29.03.14.
 */
public class ForwardingHub implements Hub {

    /**
     * List of internal web-service connections.
     */
    private ArrayList<Object> _services;

    /**
     * Application-server object
     */
    private ApplicationServer _server;


    public ForwardingHub() {

    }

    @Override
    public InternalMessage acceptNetMessage(InternalMessage message, OutputStream streamToRequestor) {
        return null;
    }

    @Override
    public InternalMessage acceptLocalMessage(InternalMessage message) {
        return null;
    }

    @Override
    public String getInetAdress() {
        return null;
    }

    @Override
    public void registerService(ServiceConnection webServiceConnector) {

    }

    @Override
    public void removeService(ServiceConnection webServiceConnector) {

    }

    @Override
    public boolean isServiceRegistered(ServiceConnection webServiceConnector) {
        return false;
    }

    @Override
    public Collection<ServiceConnection> getServices() {
        return null;
    }
}
