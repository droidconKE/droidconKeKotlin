/*
 * Copyright 2023 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android254.presentation.common.navigation

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import com.android254.presentation.common.adaptive.DroidconWindowSize
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationSuiteTypeTest {
    @Test
    fun `a phone gets the short navigation bar whatever the drawer flag says`() {
        assertEquals(NavigationSuiteType.ShortNavigationBarCompact, navigationSuiteTypeFor(DroidconWindowSize.Compact, showsDrawer = false))
        assertEquals(NavigationSuiteType.ShortNavigationBarCompact, navigationSuiteTypeFor(DroidconWindowSize.Compact, showsDrawer = true))
    }

    @Test
    fun `a tall wide window gets the expanded rail, which is the type that draws the logo header`() {
        assertEquals(NavigationSuiteType.WideNavigationRailExpanded, navigationSuiteTypeFor(DroidconWindowSize.Expanded, showsDrawer = true))
    }

    @Test
    fun `other wider windows get the collapsed rail`() {
        assertEquals(NavigationSuiteType.WideNavigationRailCollapsed, navigationSuiteTypeFor(DroidconWindowSize.Medium, showsDrawer = false))
        assertEquals(NavigationSuiteType.WideNavigationRailCollapsed, navigationSuiteTypeFor(DroidconWindowSize.Expanded, showsDrawer = false))
    }
}