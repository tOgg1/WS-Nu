package org.ntnunotif.wsnu.base.net;

import javax.xml.namespace.NamespaceContext;
import java.util.*;

/**
 * Created by Inge on 29.04.2014.
 */
public class NuNamespaceContextResolver {

    private Map<Object, ScopeLevelBindings> scopeLevelBindingsMap = new HashMap<>();
    private ScopeLevelBindings currentScope = null;

    public NamespaceContext resolveNamespaceContext(Object object) {
        if (scopeLevelBindingsMap.containsKey(object)) {
            return new NuResolvedNamespaceContext(scopeLevelBindingsMap.get(object));
        }
        return null;
    }

    public void registerObjectWithCurrentNamespaceScope(Object object) {
        if (currentScope == null) {
            throw new IllegalStateException("No scopes open, can not register binding!");
        }
        scopeLevelBindingsMap.put(object, currentScope);
    }

    public void openScope() {
        ScopeLevelBindings newScope = new ScopeLevelBindings();
        newScope.parent = currentScope;
        currentScope = newScope;
    }

    public void closeScope() {
        if (currentScope != null) {
            currentScope = currentScope.parent;
        } else {
            throw new IllegalStateException("No scopes open, can not close scope!");
        }
    }

    public void putNamespaceBinding(String prefix, String namespace) {

        if (currentScope == null) {
            throw new IllegalStateException("No scopes open, can not register binding!");
        }

        if (currentScope.bindings == null) {
            currentScope.bindings = new HashMap<>();
        }

        currentScope.bindings.put(prefix, namespace);
    }

    private static class ScopeLevelBindings {
        ScopeLevelBindings parent;
        Map<String, String> bindings;
    }

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
