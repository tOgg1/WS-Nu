package org.ntnunotif.wsnu.base.net;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Inge on 29.04.2014.
 */
public class NuNamespaceContextResolver {
    private List<NuNamespaceContext> contexts = new ArrayList<>();
    private Map<String, String> currentScope = null;

    public NamespaceContext resolveNamespaceContext(Object object) {
        // TODO
        return null;
    }

    public void registerObjectWithCurrentNamespaceScope(Object object) {
        // TODO
    }

    public void openScope() {
        // TODO
    }

    public void closeScope() {
        // TODO
    }

    public void putNamespaceBinding(String prefix, String namespace) {
        if (currentScope == null) {
            currentScope = new HashMap<>();
        }
        currentScope.put(prefix, namespace);
    }

    private static class ScopeLevelBindings {
        int scope;
        Map<String, String> bindings;
    }
}
