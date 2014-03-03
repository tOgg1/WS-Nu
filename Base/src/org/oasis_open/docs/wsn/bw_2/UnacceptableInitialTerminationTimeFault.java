
package org.oasis_open.docs.wsn.bw_2;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.7.10
 * 2014-03-03T11:23:48.490+01:00
 * Generated source version: 2.7.10
 */

@WebFault(name = "UnacceptableInitialTerminationTimeFault", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
public class UnacceptableInitialTerminationTimeFault extends Exception {
    
    private org.oasis_open.docs.wsn.b_2.UnacceptableInitialTerminationTimeFaultType unacceptableInitialTerminationTimeFault;

    public UnacceptableInitialTerminationTimeFault() {
        super();
    }
    
    public UnacceptableInitialTerminationTimeFault(String message) {
        super(message);
    }
    
    public UnacceptableInitialTerminationTimeFault(String message, Throwable cause) {
        super(message, cause);
    }

    public UnacceptableInitialTerminationTimeFault(String message, org.oasis_open.docs.wsn.b_2.UnacceptableInitialTerminationTimeFaultType unacceptableInitialTerminationTimeFault) {
        super(message);
        this.unacceptableInitialTerminationTimeFault = unacceptableInitialTerminationTimeFault;
    }

    public UnacceptableInitialTerminationTimeFault(String message, org.oasis_open.docs.wsn.b_2.UnacceptableInitialTerminationTimeFaultType unacceptableInitialTerminationTimeFault, Throwable cause) {
        super(message, cause);
        this.unacceptableInitialTerminationTimeFault = unacceptableInitialTerminationTimeFault;
    }

    public org.oasis_open.docs.wsn.b_2.UnacceptableInitialTerminationTimeFaultType getFaultInfo() {
        return this.unacceptableInitialTerminationTimeFault;
    }
}
