//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

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
