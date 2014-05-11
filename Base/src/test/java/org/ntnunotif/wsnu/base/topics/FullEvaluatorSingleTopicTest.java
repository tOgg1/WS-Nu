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

package org.ntnunotif.wsnu.base.topics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.NuNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 06.04.2014.
 */
public class FullEvaluatorSingleTopicTest {
    private NamespaceContext namespaceContext;

    @Before
    public void setUp() {
        NuNamespaceContext context = new NuNamespaceContext();
        context.put("test", "http://test.com");
        context.put("ex", "http://example.org");
        namespaceContext = context;
    }

    @Test
    public void testSimpleTopicTranslation() throws Exception{
        List<QName> expected = new ArrayList<>();
        expected.add(new QName("http://example.org", "root"));
        List<QName> qNames = FullEvaluator.evaluateFullTopicExpressionToQNameList("ex:root", namespaceContext);
        Assert.assertEquals("Simple expression failed", expected, qNames);
    }

    @Test
    public void testLongerTopicTranslation() throws Exception{
        List<QName> expected = new ArrayList<>();
        expected.add(new QName("http://example.org", "root"));
        expected.add(new QName("haha"));
        expected.add(new QName("http://test.com", "b"));
        List<QName> qNames = FullEvaluator.evaluateFullTopicExpressionToQNameList("ex:root/haha/test:b", namespaceContext);
        Assert.assertEquals("Simple expression failed", expected, qNames);
    }
}
