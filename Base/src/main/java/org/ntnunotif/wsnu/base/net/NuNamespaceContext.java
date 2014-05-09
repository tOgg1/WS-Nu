package org.ntnunotif.wsnu.base.net;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A <code>NuNamespaceContext</code> is a very simple {@link javax.xml.namespace.NamespaceContext} which support only
 * prefix to namespace resolution. It is backed up by a {@link java.util.HashMap}
 */
public class NuNamespaceContext implements NamespaceContext {

    private Map<String, String> table = new HashMap<>();

    /**
     * Creates an empty context (no bindings)
     */
    public NuNamespaceContext() {
    }

    /**
     * Creates a copy of the original context. The exact same bindings are retained in the new one, but changes in one
     * after creation does not reflect on its counterpart.
     *
     * @param original the context to copy
     */
    public NuNamespaceContext(NuNamespaceContext original) {
        this.table.putAll(original.table);
    }

    /**
     * Binds a prefix to a namespace
     *
     * @param prefix    the prefix to bind
     * @param nameSpace the namespace to bind
     */
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
        for (Map.Entry e : table.entrySet()) {
            outString += "\n" + e.getKey() + "\t:\t" + e.getValue();
        }
        return outString;
    }

    @Override
    public NuNamespaceContext clone() {
        return new NuNamespaceContext(this);
    }
}
