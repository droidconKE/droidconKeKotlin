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
    @Test
    fun `each scene holds a logo, twists the cube as it turns to the next, then solves it facing the viewer`() =
        runTest {
            val motion = CubeMotion()
            backgroundScope.launch(FakeFrameClock(testScheduler)) { motion.run() }

            advanceTimeBy(2_000)
            assertTrue(motion.state.isSolved)
            assertEquals(CubeFace.Front, motion.facing())

            advanceTimeBy(1_500)
            assertFalse(motion.state.isSolved)

            advanceTimeBy(3_000)
            assertTrue(motion.state.isSolved)
            assertNull(motion.turn)
            assertEquals(CubeFace.Right, motion.facing())
        }

    @Test
    fun `the cube shows every logo in turn and comes back to the first`() =
        runTest {
            val motion = CubeMotion()
            backgroundScope.launch(FakeFrameClock(testScheduler)) { motion.run() }
            val seen = mutableListOf<CubeFace>()

            var samples = 0
            while (seen.size <= Showcase.size && samples++ < MAX_SAMPLES) {
                advanceTimeBy(SAMPLE_MILLIS)
                if (motion.state.isSolved && motion.turn == null) {
                    motion.facing().takeIf { seen.lastOrNull() != it }?.let(seen::add)
                }
            }

            assertEquals(Showcase + Showcase.first(), seen)
        }

    @Test
    fun `with animations off the cube rests solved, showing the front`() =
        runTest {
            val motion = CubeMotion()
            backgroundScope.launch(FakeFrameClock(testScheduler) + NoMotion) { motion.run() }

            advanceTimeBy(20_000)

            assertRestingSolved(motion)
        }

    @Test
    fun `turning animations off mid-run brings the cube back to rest`() =
        runTest {
            val motion = CubeMotion()
            val scale = SwitchableMotion()
            backgroundScope.launch(FakeFrameClock(testScheduler) + scale) { motion.run() }
            advanceTimeBy(3_500)
            assertFalse(motion.state.isSolved)

            scale.scaleFactor = 0f
            advanceTimeBy(8_000)

            assertRestingSolved(motion)
        }

    @Test
    fun `rest returns a moving cube to its solved resting pose`() =
        runTest {
            val motion = CubeMotion()
            val job = backgroundScope.launch(FakeFrameClock(testScheduler)) { motion.run() }
            advanceTimeBy(3_500)
            job.cancel()

            motion.rest()

            assertRestingSolved(motion)
        }

    @Test
    fun `cancelled mid-turn, the cube is made whole and carries on when run again`() =
        runTest {
            val motion = CubeMotion()
            val first = backgroundScope.launch(FakeFrameClock(testScheduler)) { motion.run() }
            advanceTimeBy(3_000)
            first.cancel()
            advanceTimeBy(1)
            assertNull(motion.turn)

            backgroundScope.launch(FakeFrameClock(testScheduler)) { motion.run() }
            advanceTimeBy(1)
            assertTrue(motion.state.isSolved)
            advanceTimeBy(3_500)
            assertFalse(motion.state.isSolved)
            advanceTimeBy(3_000)
            assertTrue(motion.state.isSolved)
        }

    private fun assertRestingSolved(motion: CubeMotion) {
        assertTrue(motion.state.isSolved)
        assertNull(motion.turn)
        assertEquals(presenting(CubeFace.Front), motion.orientation)
    }

    /** The face turned most towards the viewer. */
    private fun CubeMotion.facing() =
        state
            .visibleQuads(orientation)
            .groupBy { it.face }
            .maxBy { (_, quads) -> quads.sumOf { it.light.toDouble() } }
            .key

    private class FakeFrameClock(
        private val scheduler: TestCoroutineScheduler,
    ) : MonotonicFrameClock {
        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
            delay(FRAME_MILLIS)
            return onFrame(scheduler.currentTime * NANOS_PER_MILLI)
        }
    }

    private class SwitchableMotion : MotionDurationScale {
        override var scaleFactor = 1f
    }

    private object NoMotion : MotionDurationScale {
        override val scaleFactor = 0f
    }

    private companion object {
        const val FRAME_MILLIS = 16L
        const val NANOS_PER_MILLI = 1_000_000L
        const val SAMPLE_MILLIS = 250L
        const val MAX_SAMPLES = 1_000
    }
}