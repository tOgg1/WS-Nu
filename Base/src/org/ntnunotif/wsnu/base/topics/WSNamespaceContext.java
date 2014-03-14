package org.ntnunotif.wsnu.base.topics;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Inge on 14.03.14.
 */
public class WSNamespaceContext implements NamespaceContext {

    private Map<String, String> table = new HashMap<>();

    public synchronized void put(String prefix, String nameSpace) {
        table.put(prefix, nameSpace);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return table.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException("WSNamespaceContext does not support namespaceURI resolution");
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException("WSNamespaceContext does not support namespaceURI resolution");
    }
}
