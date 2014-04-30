package org.ntnunotif.wsnu.base.net;

import javax.xml.namespace.NamespaceContext;
import java.util.*;

/**
 * Created by Inge on 29.04.2014.
 */
public class NuNamespaceContextResolver {

    private Map<Object, ScopeLevelBindings> scopeLevelBindingsMap = new HashMap<>();
    private ScopeLevelBindings currentScope = null;

    /**
     * Gets the namespace connected to the object given.
     *
     * @param object the object to resolve context for
     * @return a valid {@link javax.xml.namespace.NamespaceContext} if the object is known to this resolver. <code>null</code> otherwise.
     */
    public NamespaceContext resolveNamespaceContext(Object object) {
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
     * Private class representing the actual namespaces that are returned.
     */
    private static class NuResolvedNamespaceContext implements NamespaceContext {

        final ScopeLevelBindings scopeSource;

        NuResolvedNamespaceContext(ScopeLevelBindings scopeSource) {
            this.scopeSource = scopeSource;
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
