package ntnunotif.wsnu.base.net;

import junit.framework.TestCase;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.InternalMessage;

import static org.ntnunotif.wsnu.base.internal.InternalMessage.*;

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

        assertTrue((STATUS_OK & messageOk.statusCode) > 0);
        assertTrue((STATUS_FAULT & messageFault.statusCode) > 0);
        assertTrue((STATUS_INVALID_DESTINATION & messageOk.statusCode) > 0);

        assertTrue((STATUS_OK & messageOk.statusCode) == 0);

        /* Test various fault-flags */
        InternalMessage messageInternalFault = new InternalMessage(STATUS_FAULT_INTERNAL_ERROR, null);
        InternalMessage messageUnknownFault = new InternalMessage(STATUS_FAULT_UNKNOWN_METHOD, null);

        assertTrue((STATUS_FAULT & messageInternalFault.statusCode) > 0);
        assertTrue((STATUS_FAULT_INVALID_PAYLOAD & messageInternalFault.statusCode) == 0);

        /* Test multiple codes */
    }
}
