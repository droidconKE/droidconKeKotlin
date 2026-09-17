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

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.view.WindowCompat
import com.android254.presentation.common.insets.StatusBarIconAppearance
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StatusBarIconAppearanceTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `follows the surface the screen draws behind the status bar`() {
        var overHero by mutableStateOf(true)

        composeTestRule.setContent {
            StatusBarIconAppearance(darkIcons = !overHero)
        }
        composeTestRule.waitForIdle()

        assertFalse("a dark hero needs light icons", lightStatusBars())

        overHero = false
        composeTestRule.waitForIdle()

        assertTrue("the collapsed light bar needs dark icons", lightStatusBars())
    }

    @Test
    fun `restores the previous appearance when the screen leaves`() {
        setLightStatusBars(true)
        var onScreen by mutableStateOf(true)

        composeTestRule.setContent {
            if (onScreen) StatusBarIconAppearance(darkIcons = false)
        }
        composeTestRule.waitForIdle()
        assertFalse(lightStatusBars())

        onScreen = false
        composeTestRule.waitForIdle()

        assertTrue("leaving the screen must not strand the next one", lightStatusBars())
    }

    private fun controller() =
        composeTestRule.activity.let {
            WindowCompat.getInsetsController(it.window, it.window.decorView)
        }

    private fun lightStatusBars(): Boolean = controller().isAppearanceLightStatusBars

    private fun setLightStatusBars(value: Boolean) {
        composeTestRule.runOnUiThread { controller().isAppearanceLightStatusBars = value }
    }
}