package org.ntnunotif.wsnu.base.internal;

/**
 * Created by tormod on 3/11/14.
 */
public class GenericConnector implements WebServiceConnection{

    /**
     * Default constructor
     */
    public GenericConnector() {

    }

    @Override
    public void acceptMessage(Object message) {

    }

    @Override
    public int getServiceType() {
        return 0;
    }

    @Override
    public int getServiceFunctionality() {
        return 0;
    }
}
