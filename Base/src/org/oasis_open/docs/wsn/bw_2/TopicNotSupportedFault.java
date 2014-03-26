
package org.oasis_open.docs.wsn.bw_2;

import javax.xml.ws.WebFault;


/**
 * This class was org.generated by Apache CXF 2.7.10
 * 2014-03-03T11:43:03.835+01:00
 * Generated source version: 2.7.10
 */

@WebFault(name = "TopicNotSupportedFault", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
public class TopicNotSupportedFault extends Exception {
    
    private org.oasis_open.docs.wsn.b_2.TopicNotSupportedFaultType topicNotSupportedFault;

    public TopicNotSupportedFault() {
        super();
    }
    
    public TopicNotSupportedFault(String message) {
        super(message);
    }
    
    public TopicNotSupportedFault(String message, Throwable cause) {
        super(message, cause);
    }

    public TopicNotSupportedFault(String message, org.oasis_open.docs.wsn.b_2.TopicNotSupportedFaultType topicNotSupportedFault) {
        super(message);
        this.topicNotSupportedFault = topicNotSupportedFault;
    }

    public TopicNotSupportedFault(String message, org.oasis_open.docs.wsn.b_2.TopicNotSupportedFaultType topicNotSupportedFault, Throwable cause) {
        super(message, cause);
        this.topicNotSupportedFault = topicNotSupportedFault;
    }

    public org.oasis_open.docs.wsn.b_2.TopicNotSupportedFaultType getFaultInfo() {
        return this.topicNotSupportedFault;
    }
}
