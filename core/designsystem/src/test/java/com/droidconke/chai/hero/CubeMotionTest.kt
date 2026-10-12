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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CubeMotionTest {
    private val turn = tween<Float>(durationMillis = 100, easing = LinearEasing)

    @Test
    fun `the logos hold, break up as the cube scrambles, then come back as it solves`() =
        runTest {
            val motion = CubeMotion()
            backgroundScope.launch(FakeFrameClock(testScheduler)) { motion.run(turn) }

            advanceTimeBy(5_900)
            assertTrue(motion.state.isSolved)
            assertTrue(motion.yaw > 0f)

            advanceTimeBy(600)
            assertFalse(motion.state.isSolved)

            advanceTimeBy(5_000)
            assertTrue(motion.state.isSolved)
        }

    @Test
    fun `with animations off the cube rests solved at the resting pose`() =
        runTest {
            val motion = CubeMotion()
            backgroundScope.launch(FakeFrameClock(testScheduler) + NoMotion) { motion.run(turn) }

            advanceTimeBy(20_000)

            assertTrue(motion.state.isSolved)
            assertNull(motion.turn)
            assertEquals(0f, motion.yaw)
            assertEquals(0f, motion.pitch)
        }

    @Test
    fun `cancelled mid-turn, the cube is left whole and carries on when run again`() =
        runTest {
            val motion = CubeMotion()
            val first = backgroundScope.launch(FakeFrameClock(testScheduler)) { motion.run(turn) }
            advanceTimeBy(6_050)
            first.cancel()
            advanceTimeBy(1)

            assertNull(motion.turn)
            assertTrue(motion.state.isSolved)

            backgroundScope.launch(FakeFrameClock(testScheduler)) { motion.run(turn) }
            advanceTimeBy(6_500)
            assertFalse(motion.state.isSolved)
            advanceTimeBy(5_000)
            assertTrue(motion.state.isSolved)
        }

    private class FakeFrameClock(
        private val scheduler: TestCoroutineScheduler,
    ) : MonotonicFrameClock {
        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
            delay(FRAME_MILLIS)
            return onFrame(scheduler.currentTime * NANOS_PER_MILLI)
        }
    }

    private object NoMotion : MotionDurationScale {
        override val scaleFactor = 0f
    }

    private companion object {
        const val FRAME_MILLIS = 16L
        const val NANOS_PER_MILLI = 1_000_000L
    }
}