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
package com.android254.presentation.sessions.components

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.DroidconAppBarWithFilter
import com.droidconke.chai.ChaiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppBarTargetsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val clicks = mutableListOf<String>()

    private fun showFilterBar(isFilterActive: Boolean) {
        composeTestRule.setContent {
            ChaiTheme {
                DroidconAppBarWithFilter(
                    isListActive = true,
                    onListIconClick = { clicks += "list" },
                    onAgendaIconClick = { clicks += "agenda" },
                    isFilterActive = isFilterActive,
                    showLogo = true,
                    onFilterButtonClick = { clicks += "filter" },
                )
            }
        }
    }

    @Test
    fun `the filter opens before any filter is applied`() {
        showFilterBar(isFilterActive = false)

        composeTestRule.onNodeWithText("Filter").performClick()

        assertEquals(listOf("filter"), clicks)
    }

    @Test
    fun `an applied filter is announced, not only coloured`() {
        showFilterBar(isFilterActive = true)

        composeTestRule
            .onNodeWithText("Filter")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Filters applied"))
    }

    @Test
    fun `the layout icons are labelled and say which is selected`() {
        showFilterBar(isFilterActive = false)

        composeTestRule.onNodeWithContentDescription("List view").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Agenda view").assertIsNotSelected().performClick()

        assertEquals(listOf("agenda"), clicks)
    }

    @Test
    fun `every filter bar control is at least 48dp square`() {
        showFilterBar(isFilterActive = false)

        listOf(
            composeTestRule.onNodeWithContentDescription("List view"),
            composeTestRule.onNodeWithContentDescription("Agenda view"),
            composeTestRule.onNodeWithText("Filter"),
        ).forEach { it.assertTouchTargetAtLeast48dp() }
    }

    private fun SemanticsNodeInteraction.assertTouchTargetAtLeast48dp() {
        val bounds = fetchSemanticsNode().touchBoundsInRoot
        val min = with(composeTestRule.density) { 48.dp.toPx() }
        assertTrue("touch target ${bounds.width}x${bounds.height}px < ${min}px", bounds.width >= min && bounds.height >= min)
    }
}