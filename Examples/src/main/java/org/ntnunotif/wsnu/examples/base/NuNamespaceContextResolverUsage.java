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

package org.ntnunotif.wsnu.examples.base;

import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;

import javax.xml.namespace.NamespaceContext;

/**
 * Shows how to use a {@link org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver}.
 */
public class NuNamespaceContextResolverUsage {

    public static void main(String[] args) {
        // Create some objects that shall be bound to contexts, and not
        Object bound1 = new Object();
        Object bound2 = new Object();
        Object bound3 = new Object();
        Object unbound = new Object();

        // Create Strings that will serve as out prefixes and namespaces
        String prefix1 = "prefix1";
        String prefix2 = "prefix2";
        String namespace1 = "http://example.com/bound1";
        String namespace2 = "http://example.com/bound2";

        // Create a new resolver
        NuNamespaceContextResolver namespaceContextResolver = new NuNamespaceContextResolver();

        // Bindings can not be done before a scope is declared
        try {
            namespaceContextResolver.putNamespaceBinding("prefix", "namespace");
            System.err.println("We should not get here, an IllegalStateException should be called");
        } catch (IllegalStateException e) {
            System.out.println("This IllegalStateException is expected");
        }
        try {
            namespaceContextResolver.registerObjectWithCurrentNamespaceScope(unbound);
            System.err.println("We should not get here, an IllegalStateException should be called");
        } catch (IllegalStateException e) {
            System.out.println("This IllegalStateException is expected");
        }

        // Scopes must be opened before they are closed:
        try {
            namespaceContextResolver.closeScope();
            System.err.println("We should not see this message in the output");
        } catch (IllegalStateException e) {
            System.out.println("This IllegalStateException is expected");
        }

        // Let us open a scope
        namespaceContextResolver.openScope();

        // Now we can bind objects
        namespaceContextResolver.registerObjectWithCurrentNamespaceScope(bound1);

        // And prefixes
        namespaceContextResolver.putNamespaceBinding(prefix1, namespace1);

        // It does not matter which order we bind objects
        namespaceContextResolver.registerObjectWithCurrentNamespaceScope(bound2);

        // We can open more scopes with additional bindings
        namespaceContextResolver.openScope();
        namespaceContextResolver.putNamespaceBinding(prefix2, namespace2);
        namespaceContextResolver.registerObjectWithCurrentNamespaceScope(bound3);

        // We can even close the scopes
        namespaceContextResolver.closeScope();
        namespaceContextResolver.closeScope();

        // but not more than are constructed
        try {
            namespaceContextResolver.closeScope();
            System.err.println("We should not get here");
        } catch (IllegalStateException e) {
            System.out.println("This exception is expected");
        }

        // We can now look up the different namespaces
        NamespaceContext context1 = namespaceContextResolver.resolveNamespaceContext(bound1);
        NamespaceContext context2 = namespaceContextResolver.resolveNamespaceContext(bound2);
        NamespaceContext context3 = namespaceContextResolver.resolveNamespaceContext(bound3);
        NamespaceContext unboundContext = namespaceContextResolver.resolveNamespaceContext(unbound);

        // The contexts 1 and 2 contain only one prefix
        if (context1.getNamespaceURI(prefix1).equals(namespace1)) {
             System.out.println("context one contained prefix 1");
        } else {
            System.err.println("context one should contain this prefix");
        }

        if (context1.getNamespaceURI(prefix2) == null) {
            System.out.println("context one did not contain prefix 2");
        } else {
            System.err.println("context one should not contain this prefix");
        }
        // The returned namespace is indeed the same object that we put in
        if (context2.getNamespaceURI(prefix1).equals(namespace1)) {
            System.out.println("context two contained prefix 1");
        } else {
            System.err.println("context two should contain this prefix");
        }

        if (context1.getNamespaceURI(prefix2) == null) {
            System.out.println("context two did not contain prefix 2");
        } else {
            System.err.println("context two should not contain this prefix");
        }

        // the third context on the other hand contains both prefixes
        if (context3.getNamespaceURI(prefix1).equals(namespace1)) {
            System.out.println("context three contained prefix 1");
        } else {
            System.err.println("context three should contain this prefix");
        }

        if (context3.getNamespaceURI(prefix2).equals(namespace2)) {
            System.out.println("context three contained prefix 2");
        } else {
            System.err.println("context three should contain this prefix");
        }

        // The unbound object did not resolve to any namespace context
        if (unboundContext == null) {
            System.out.println("Unbound objects have null namespace contexts");
        } else {
            System.err.println("This should be null");
        }
    }
}
