
package org.oasis_open.docs.wsn.bw_2;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.7.10
 * 2014-03-03T11:23:48.449+01:00
 * Generated source version: 2.7.10
 */

@WebFault(name = "UnrecognizedPolicyRequestFault", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
public class UnrecognizedPolicyRequestFault extends Exception {
    
    private org.oasis_open.docs.wsn.b_2.UnrecognizedPolicyRequestFaultType unrecognizedPolicyRequestFault;

    public UnrecognizedPolicyRequestFault() {
        super();
    }
    
    public UnrecognizedPolicyRequestFault(String message) {
        super(message);
    }
    
    public UnrecognizedPolicyRequestFault(String message, Throwable cause) {
        super(message, cause);
    }

    public UnrecognizedPolicyRequestFault(String message, org.oasis_open.docs.wsn.b_2.UnrecognizedPolicyRequestFaultType unrecognizedPolicyRequestFault) {
        super(message);
        this.unrecognizedPolicyRequestFault = unrecognizedPolicyRequestFault;
    }

    public UnrecognizedPolicyRequestFault(String message, org.oasis_open.docs.wsn.b_2.UnrecognizedPolicyRequestFaultType unrecognizedPolicyRequestFault, Throwable cause) {
        super(message, cause);
        this.unrecognizedPolicyRequestFault = unrecognizedPolicyRequestFault;
    }

    public org.oasis_open.docs.wsn.b_2.UnrecognizedPolicyRequestFaultType getFaultInfo() {
        return this.unrecognizedPolicyRequestFault;
    }
}
