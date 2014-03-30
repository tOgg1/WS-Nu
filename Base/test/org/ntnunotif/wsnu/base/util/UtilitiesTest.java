package org.ntnunotif.wsnu.base.util;

import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.MappingConnector;

import java.lang.reflect.Field;
import java.util.List;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Created by tormod on 25.03.14.
 */
public class UtilitiesTest{

    @Test
    public void testGetAllFields() throws Exception {
        List<Field> fields = (List<Field>)Utilities.getFieldsUpTo(MappingConnector.class, null);

        for (Field field : fields) {
            System.out.println(field.getName());
        }
    }

    @Test
    public void testBits() throws Exception {
        InternalMessage messageOne = new InternalMessage(STATUS_OK | STATUS_HAS_MESSAGE, 0);

        int bitOne = messageOne.statusCode;
        int bitTwo = (messageOne.statusCode ^ (STATUS_OK | STATUS_HAS_MESSAGE)) & (STATUS_OK | STATUS_HAS_MESSAGE);

        System.out.println(bitOne);
        System.out.println(bitTwo);


    }
}
