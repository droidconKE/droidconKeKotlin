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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.DroidconAppBar
import com.android254.presentation.common.components.DroidconAppBarWithFeedbackButton
import com.android254.presentation.common.components.DroidconAppBarWithFilter
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import org.junit.Test

/** Robolectric reports no system bars, so the inset has to be passed in to be seen at all. */
class AppBarInsetsScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun `plain app bar under a display cutout`() {
        captureComponent("app_bar_plain_cutout") {
            DroidconAppBar(windowInsets = WindowInsets(top = CUTOUT))
        }
    }

    @Test
    fun `feedback app bar under a display cutout`() {
        captureComponent("app_bar_feedback_cutout") {
            DroidconAppBarWithFeedbackButton(
                onButtonClick = {},
                windowInsets = WindowInsets(top = CUTOUT),
            )
        }
    }

    @Test
    fun `filter app bar under a display cutout`() {
        captureComponent("app_bar_filter_cutout") {
            DroidconAppBarWithFilter(
                isListActive = true,
                onListIconClick = {},
                onAgendaIconClick = {},
                isFilterActive = true,
                onFilterButtonClick = {},
                windowInsets = WindowInsets(top = CUTOUT),
            )
        }
    }

    private companion object {
        val CUTOUT: Dp = 48.dp
    }
}