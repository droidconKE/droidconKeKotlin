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
package com.droidconke.chai.hero

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CubeHeroPauseTest {
    @get:Rule
    val rule = createComposeRule()

    private val owner = FakeLifecycleOwner()
    private val motion = CubeMotion()

    @Test
    fun `the cube moves only while the app is resumed`() {
        show()
        rule.mainClock.advanceTimeBy(1_000)
        assertNotEquals(presenting(CubeFace.Front), motion.orientation)

        settle { owner.registry.currentState = Lifecycle.State.STARTED }
        val paused = motion.orientation
        rule.mainClock.advanceTimeBy(1_000)
        assertEquals(paused, motion.orientation)

        settle { owner.registry.currentState = Lifecycle.State.RESUMED }
        rule.mainClock.advanceTimeBy(500)
        assertNotEquals(paused, motion.orientation)
    }

    @Test
    fun `turning the cube off while paused still rests it`() {
        var animate by mutableStateOf(true)
        show { animate }
        rule.mainClock.advanceTimeBy(3_500)
        settle { owner.registry.currentState = Lifecycle.State.STARTED }

        settle { animate = false }

        assertTrue(motion.state.isSolved)
        assertNull(motion.turn)
        assertEquals(presenting(CubeFace.Front), motion.orientation)
    }

    private fun settle(change: () -> Unit) {
        rule.runOnIdle(change)
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        rule.waitForIdle()
    }

    private fun show(animate: () -> Boolean = { true }) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                CubeHero(motion, Modifier.size(200.dp), animate())
            }
        }
    }

    private class FakeLifecycleOwner : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
        override val lifecycle: Lifecycle get() = registry
    }

    private companion object {
        const val SETTLE_MILLIS = 100L
    }
}