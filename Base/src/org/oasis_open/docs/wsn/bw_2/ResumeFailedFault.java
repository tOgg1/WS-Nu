
package org.oasis_open.docs.wsn.bw_2;

import javax.xml.ws.WebFault;


/**
 * This class was org.generated by Apache CXF 2.7.10
 * 2014-03-03T11:43:03.951+01:00
 * Generated source version: 2.7.10
 */

@WebFault(name = "ResumeFailedFault", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
public class ResumeFailedFault extends Exception {
    
    private org.oasis_open.docs.wsn.b_2.ResumeFailedFaultType resumeFailedFault;

    public ResumeFailedFault() {
        super();
    }
    
    public ResumeFailedFault(String message) {
        super(message);
    }
    
    public ResumeFailedFault(String message, Throwable cause) {
        super(message, cause);
    }

    public ResumeFailedFault(String message, org.oasis_open.docs.wsn.b_2.ResumeFailedFaultType resumeFailedFault) {
        super(message);
        this.resumeFailedFault = resumeFailedFault;
    }

    public ResumeFailedFault(String message, org.oasis_open.docs.wsn.b_2.ResumeFailedFaultType resumeFailedFault, Throwable cause) {
        super(message, cause);
        this.resumeFailedFault = resumeFailedFault;
    }

    public org.oasis_open.docs.wsn.b_2.ResumeFailedFaultType getFaultInfo() {
        return this.resumeFailedFault;
    }
}
