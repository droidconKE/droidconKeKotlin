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
package com.android254.presentation.insets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.DroidconAppBar
import com.android254.presentation.common.components.DroidconAppBarWithFeedbackButton
import com.android254.presentation.common.components.DroidconAppBarWithFilter
import com.droidconke.chai.ChaiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppBarWindowInsetsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `plain app bar reserves the status bar`() {
        assertOccupies(BAR_HEIGHT + STATUS_BAR) {
            DroidconAppBar(windowInsets = WindowInsets(top = STATUS_BAR))
        }
    }

    @Test
    fun `feedback app bar reserves the status bar`() {
        assertOccupies(BAR_HEIGHT + STATUS_BAR) {
            DroidconAppBarWithFeedbackButton(
                onButtonClick = {},
                windowInsets = WindowInsets(top = STATUS_BAR),
            )
        }
    }

    @Test
    fun `filter app bar reserves the status bar`() {
        assertOccupies(BAR_HEIGHT + STATUS_BAR) {
            DroidconAppBarWithFilter(
                isListActive = true,
                onListIconClick = {},
                onAgendaIconClick = {},
                isFilterActive = true,
                onFilterButtonClick = {},
                windowInsets = WindowInsets(top = STATUS_BAR),
            )
        }
    }

    @Test
    fun `app bar takes no extra height where there is no status bar`() {
        assertOccupies(BAR_HEIGHT) {
            DroidconAppBar(windowInsets = WindowInsets(top = 0.dp))
        }
    }

    /**
     * Measured on a wrapper rather than on the bar's own test tag, which sits inside the
     * bar's padding and so cannot see the space the bar reserves above itself.
     */
    private fun assertOccupies(
        height: Dp,
        bar: @Composable () -> Unit,
    ) {
        composeTestRule.setContent {
            ChaiTheme {
                Box(modifier = Modifier.testTag(BAR_SLOT)) { bar() }
            }
        }

        composeTestRule.onNodeWithTag(BAR_SLOT).assertHeightIsEqualTo(height)
    }

    private companion object {
        val BAR_HEIGHT: Dp = 64.dp
        val STATUS_BAR: Dp = 48.dp

        const val BAR_SLOT = "app_bar_slot"
    }
}