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
package com.android254.presentation.activity

import androidx.compose.ui.input.key.KeyEvent
import com.android254.presentation.common.navigation.Screens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import android.view.KeyEvent as AndroidKeyEvent

@RunWith(RobolectricTestRunner::class)
class RootKeyboardShortcutsTest {
    @Test
    fun `ctrl plus 1 through 4 navigates to top level destinations`() {
        val navigated = mutableListOf<Screens>()
        val keys =
            listOf(
                AndroidKeyEvent.KEYCODE_1,
                AndroidKeyEvent.KEYCODE_2,
                AndroidKeyEvent.KEYCODE_3,
                AndroidKeyEvent.KEYCODE_4,
            )

        keys.forEach { keyCode ->
            val event = KeyEvent(AndroidKeyEvent(0, 0, AndroidKeyEvent.ACTION_DOWN, keyCode, 0, AndroidKeyEvent.META_CTRL_ON))
            assertTrue(handleRootKeyEvent(event) { navigated += it })
        }

        assertEquals(
            listOf(Screens.Home, Screens.Feed, Screens.Sessions, Screens.About),
            navigated,
        )
    }

    @Test
    fun `ignores key up, repeated key down, and unmodified digits`() {
        val navigated = mutableListOf<Screens>()
        val keyUp = KeyEvent(AndroidKeyEvent(0, 0, AndroidKeyEvent.ACTION_UP, AndroidKeyEvent.KEYCODE_1, 0, AndroidKeyEvent.META_CTRL_ON))
        val repeat = KeyEvent(AndroidKeyEvent(0, 0, AndroidKeyEvent.ACTION_DOWN, AndroidKeyEvent.KEYCODE_2, 1, AndroidKeyEvent.META_CTRL_ON))
        val noCtrl = KeyEvent(AndroidKeyEvent(0, 0, AndroidKeyEvent.ACTION_DOWN, AndroidKeyEvent.KEYCODE_3, 0, 0))

        assertFalse(handleRootKeyEvent(keyUp) { navigated += it })
        assertFalse(handleRootKeyEvent(repeat) { navigated += it })
        assertFalse(handleRootKeyEvent(noCtrl) { navigated += it })
        assertTrue(navigated.isEmpty())
    }
}