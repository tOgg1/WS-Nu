package org.ntnunotif.wsnu.base.internal;

/**
 * Created by tormod on 3/3/14.
 */
public interface WebServiceConnection {

    public void acceptMessage();
    public int getServiceType();
    public int getServiceFunctionality();
}
