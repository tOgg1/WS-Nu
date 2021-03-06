
package org.oasis_open.docs.wsn.bw_2;

import javax.xml.ws.WebFault;


/**
 * This class was org.generated by Apache CXF 2.7.10
 * 2014-03-03T11:43:03.842+01:00
 * Generated source version: 2.7.10
 */

@WebFault(name = "NoCurrentMessageOnTopicFault", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
public class NoCurrentMessageOnTopicFault extends Exception {
    
    private org.oasis_open.docs.wsn.b_2.NoCurrentMessageOnTopicFaultType noCurrentMessageOnTopicFault;

    public NoCurrentMessageOnTopicFault() {
        super();
    }
    
    public NoCurrentMessageOnTopicFault(String message) {
        super(message);
    }
    
    public NoCurrentMessageOnTopicFault(String message, Throwable cause) {
        super(message, cause);
    }

    public NoCurrentMessageOnTopicFault(String message, org.oasis_open.docs.wsn.b_2.NoCurrentMessageOnTopicFaultType noCurrentMessageOnTopicFault) {
        super(message);
        this.noCurrentMessageOnTopicFault = noCurrentMessageOnTopicFault;
    }

    public NoCurrentMessageOnTopicFault(String message, org.oasis_open.docs.wsn.b_2.NoCurrentMessageOnTopicFaultType noCurrentMessageOnTopicFault, Throwable cause) {
        super(message, cause);
        this.noCurrentMessageOnTopicFault = noCurrentMessageOnTopicFault;
    }

    public org.oasis_open.docs.wsn.b_2.NoCurrentMessageOnTopicFaultType getFaultInfo() {
        return this.noCurrentMessageOnTopicFault;
    }
}
