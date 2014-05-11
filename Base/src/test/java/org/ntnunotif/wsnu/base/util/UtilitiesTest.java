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

package org.ntnunotif.wsnu.base.util;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Test for the class {@link org.ntnunotif.wsnu.base.util.Utilities}
 */
public class UtilitiesTest {

    @Test
    public void testCountOccurences() throws Exception {
        String someString = "hithisisastringwithhihi";

        assertEquals("Expected count of 'hi's in string was wrong", 4, Utilities.countOccurences(someString, "hi"));
    }
}
