package org.ntnunotif.wsnu.base.net;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Inge on 14.03.14.
 */
public class NuNamespaceContext implements NamespaceContext {

    private Map<String, String> table = new HashMap<>();

    public NuNamespaceContext() {}

    public NuNamespaceContext(NuNamespaceContext original) {
        this.table.putAll(original.table);
    }

    public synchronized void put(String prefix, String nameSpace) {
        table.put(prefix, nameSpace);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return table.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException("NuNamespaceContext does not support namespaceURI resolution");
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException("NuNamespaceContext does not support namespaceURI resolution");
    }

    @Override
    public String toString() {
        String outString = NuNamespaceContext.class.toString();
        for (Map.Entry e: table.entrySet()) {
            outString += "\n" + e.getKey() + "\t:\t" + e.getValue();
        }
        return  outString;
    }

    @Override
    public NuNamespaceContext clone() {
        return new NuNamespaceContext(this);
    }
}
