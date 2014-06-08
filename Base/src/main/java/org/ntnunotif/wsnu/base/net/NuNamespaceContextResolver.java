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
import java.util.*;

/**
 * The <code>NuNamespaceContextResolver</code> is a class able to resolve object specific
 * {@link javax.xml.namespace.NamespaceContext}s.
 */
public class NuNamespaceContextResolver {

    private final Map<Object, ScopeLevelBindings> scopeLevelBindingsMap = new HashMap<>();
    private ScopeLevelBindings currentScope = null;

    /**
     * Gets the namespace connected to the object given.
     *
     * @param object the object to resolve context for
     * @return a valid {@link javax.xml.namespace.NamespaceContext} if the object is known to this resolver. <code>null</code> otherwise.
     */
    public NuResolvedNamespaceContext resolveNamespaceContext(Object object) {
        if (scopeLevelBindingsMap.containsKey(object)) {
            return new NuResolvedNamespaceContext(scopeLevelBindingsMap.get(object));
        }
        return null;
    }

    /**
     * Registers an object to the current scope of the resolver.
     *
     * @param object the object to bind to a {@link javax.xml.namespace.NamespaceContext}
     * @throws java.lang.IllegalStateException if no scopes are open
     */
    public void registerObjectWithCurrentNamespaceScope(Object object) {
        if (currentScope == null) {
            throw new IllegalStateException("No scopes open, can not register binding!");
        }
        scopeLevelBindingsMap.put(object, currentScope);
    }

    /**
     * Opens a new scope for {@link javax.xml.namespace.NamespaceContext}s. Must be called before any objects are registered to the resolver, and before any namespace bindings are registered
     */
    public void openScope() {
        ScopeLevelBindings newScope = new ScopeLevelBindings();
        newScope.parent = currentScope;
        currentScope = newScope;
    }

    /**
     * Closes the current scope for {@link javax.xml.namespace.NamespaceContext} resolving.
     *
     * @throws java.lang.IllegalStateException if no scopes are open
     */
    public void closeScope() {
        if (currentScope != null) {
            currentScope = currentScope.parent;
        } else {
            throw new IllegalStateException("No scopes open, can not close scope!");
        }
    }

    /**
     * Binds a prefix to a namespace in the current scope.
     *
     * @param prefix    the prefix to bind
     * @param namespace the namespace the prefix should bind to
     * @throws java.lang.IllegalStateException if no scopes are open
     */
    public void putNamespaceBinding(String prefix, String namespace) {

        if (currentScope == null) {
            throw new IllegalStateException("No scopes open, can not register binding!");
        }

        if (currentScope.bindings == null) {
            currentScope.bindings = new HashMap<>();
        }

        currentScope.bindings.put(prefix, namespace);
    }

    /**
     * Private class to keep track of scopes.
     */
    private static class ScopeLevelBindings {
        ScopeLevelBindings parent;
        Map<String, String> bindings;
    }

    /**
     * public class representing the actual namespaces that are returned.
     */
    public static class NuResolvedNamespaceContext implements NamespaceContext {

        final ScopeLevelBindings scopeSource;

        NuResolvedNamespaceContext(ScopeLevelBindings scopeSource) {
            this.scopeSource = scopeSource;
        }

        public Set<String> getAllPrefixes() {
            Set<String> prefixes = new HashSet<>();

            ScopeLevelBindings scopeLevel = scopeSource;
            while (scopeLevel != null) {
                if (scopeLevel.bindings != null) {
                    prefixes.addAll(scopeLevel.bindings.keySet());
                }
                scopeLevel = scopeLevel.parent;
            }
            return prefixes;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            ScopeLevelBindings scopeLevel = scopeSource;
            while (scopeLevel != null) {
                if (scopeLevel.bindings != null && scopeLevel.bindings.containsKey(prefix)) {
                    return scopeLevel.bindings.get(prefix);
                }
                scopeLevel = scopeLevel.parent;
            }
            return null;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            throw new UnsupportedOperationException("NuResolvedNamespaceContext does not support namespaceURI resolution");
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            throw new UnsupportedOperationException("NuResolvedNamespaceContext does not support namespaceURI resolution");
        }
    }
}
