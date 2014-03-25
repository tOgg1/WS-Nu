package org.ntnunotif.wsnu.base.util;

import junit.framework.TestCase;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.MappingConnector;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by tormod on 25.03.14.
 */
public class UtilitiesTest extends TestCase {

    @Test
    public void testGetAllFields() throws Exception {
        List<Field> fields = (List<Field>)Utilities.getFieldsUpTo(MappingConnector.class, null);

        for (Field field : fields) {
            System.out.println(field.getName());
        }
    }
}
