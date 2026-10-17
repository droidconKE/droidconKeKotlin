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
package com.android254.presentation.activity

import androidx.compose.ui.test.junit4.v2.createComposeRule
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationPermissionPromptTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val calls = mutableListOf<String>()
    private val requests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private fun setPrompt() {
        composeTestRule.setContent {
            NotificationPermissionPrompt(
                requests = requests,
                onPrompted = { calls += "prompted" },
                launch = { calls += "launch" },
            )
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `the prompt is marked as asked before the system dialog opens`() {
        setPrompt()

        requests.tryEmit(Unit)
        composeTestRule.waitForIdle()

        assertEquals(listOf("prompted", "launch"), calls)
    }

    @Test
    fun `no request means no dialog`() {
        setPrompt()

        assertEquals(emptyList<String>(), calls)
    }

    @Test
    fun `the activity no longer asks at launch`() {
        // Gradle runs unit tests with the module directory as the working directory.
        val source = File("src/main/java/com/android254/presentation/activity/MainActivity.kt").readText()

        assertFalse(source.contains("askNotificationPermission"))
    }
}