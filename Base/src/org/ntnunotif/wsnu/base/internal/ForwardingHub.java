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
