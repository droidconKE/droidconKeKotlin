/*
 * Copyright 2026 DroidconKE
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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationVisibilityTest {
    @Test
    fun `top level destinations always show the navigation area`() {
        TopLevelDestination.entries.forEach { destination ->
            assertTrue(
                "${destination.name} is a tab; hiding the navigation on it would hide the way back",
                shouldShowNavigation(destination.route, isMultiPaneWindow = false),
            )
            assertTrue(
                destination.name,
                shouldShowNavigation(destination.route, isMultiPaneWindow = true),
            )
        }
    }

    @Test
    fun `the speakers list keeps the navigation area although it is not a tab`() {
        assertTrue(shouldShowNavigation(Screens.Speakers, isMultiPaneWindow = false))
    }

    @Test
    fun `a full-screen detail hides the navigation area`() {
        assertFalse(shouldShowNavigation(Screens.SessionDetails("1"), isMultiPaneWindow = false))
        assertFalse(shouldShowNavigation(Screens.SpeakerDetails("Ada"), isMultiPaneWindow = false))
    }

    @Test
    fun `a detail that is only a pane leaves the navigation area alone`() {
        assertTrue(shouldShowNavigation(Screens.SessionDetails("1"), isMultiPaneWindow = true))
        assertTrue(shouldShowNavigation(Screens.SpeakerDetails("Ada"), isMultiPaneWindow = true))
    }

    @Test
    fun `feedback owns the window at every size`() {
        assertFalse(shouldShowNavigation(Screens.FeedBack, isMultiPaneWindow = false))
        assertFalse(shouldShowNavigation(Screens.FeedBack, isMultiPaneWindow = true))
    }

    @Test
    fun `the supporting pane stands beside content, never beside a detail or a focused task`() {
        assertTrue(shouldShowSupportingPane(Screens.Home, isMultiPaneWindow = true))
        assertTrue(shouldShowSupportingPane(Screens.Sessions, isMultiPaneWindow = true))
        assertTrue(shouldShowSupportingPane(Screens.Speakers, isMultiPaneWindow = true))
        assertFalse(shouldShowSupportingPane(Screens.SessionDetails("1"), isMultiPaneWindow = true))
        assertFalse(shouldShowSupportingPane(Screens.SpeakerDetails("Ada"), isMultiPaneWindow = true))
        assertFalse(shouldShowSupportingPane(Screens.FeedBack, isMultiPaneWindow = true))
    }

    @Test
    fun `a window with one column has no room for a supporting pane`() {
        assertFalse(shouldShowSupportingPane(Screens.Home, isMultiPaneWindow = false))
        assertFalse(shouldShowSupportingPane(Screens.Sessions, isMultiPaneWindow = false))
    }

    @Test
    fun `every detail route names the list it belongs beside`() {
        assertEquals(Screens.Sessions, Screens.SessionDetails("1").listPaneRoute)
        assertEquals(Screens.Speakers, Screens.SpeakerDetails("Ada").listPaneRoute)
    }

    @Test
    fun `a route that is not a detail names no list`() {
        listOf(
            Screens.Home,
            Screens.Feed,
            Screens.Sessions,
            Screens.About,
            Screens.Speakers,
            Screens.FeedBack,
            Screens.HappeningNow,
        ).forEach { route ->
            assertNull(route.toString(), route.listPaneRoute)
        }
    }
}