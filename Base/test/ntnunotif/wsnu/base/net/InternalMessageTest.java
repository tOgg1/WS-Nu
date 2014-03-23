package ntnunotif.wsnu.base.net;

import junit.framework.TestCase;
import org.junit.Test;
import org.ntnunotif.wsnu.base.util.InternalMessage;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Created by tormod on 3/15/14.
 */
public class InternalMessageTest extends TestCase {

    @Test
    public void testStatusCodes(){
        /* Test single codes */
        InternalMessage messageOk = new InternalMessage(STATUS_OK, null);
        InternalMessage messageFault = new InternalMessage(STATUS_FAULT, null);
        InternalMessage messageWrongEndpoint = new InternalMessage(STATUS_INVALID_DESTINATION, null);

        assertTrue((STATUS_OK & messageOk._statusCode) > 0);
        assertTrue((STATUS_FAULT & messageFault._statusCode) > 0);
        assertTrue((STATUS_INVALID_DESTINATION & messageWrongEndpoint._statusCode) > 0);

        assertTrue((STATUS_FAULT & messageOk._statusCode) == 0);

        /* Test various fault-flags */
        InternalMessage messageInternalFault = new InternalMessage(STATUS_FAULT|STATUS_FAULT_INTERNAL_ERROR, null);
        InternalMessage messageUnknownFault = new InternalMessage(STATUS_FAULT|STATUS_FAULT_UNKNOWN_METHOD, null);

        assertTrue((STATUS_FAULT & messageInternalFault._statusCode) > 0);
        assertTrue((STATUS_FAULT_INVALID_PAYLOAD & messageInternalFault._statusCode) == 0);
        assertTrue((STATUS_FAULT_UNKNOWN_METHOD & messageUnknownFault._statusCode) > 0);

        /* Test multiple codes */
        InternalMessage messageInternalFaultUnknownDestination = new InternalMessage(STATUS_FAULT|STATUS_FAULT_INTERNAL_ERROR|STATUS_INVALID_DESTINATION, null);

        assertTrue((STATUS_FAULT & messageInternalFaultUnknownDestination._statusCode) > 0);
        assertTrue((STATUS_FAULT_INTERNAL_ERROR & messageInternalFaultUnknownDestination._statusCode) > 0);
        assertTrue((STATUS_INVALID_DESTINATION & messageInternalFaultUnknownDestination._statusCode) > 0);
        assertTrue(((STATUS_FAULT | STATUS_FAULT_INTERNAL_ERROR) & messageInternalFaultUnknownDestination._statusCode) > 0);
    }
}
