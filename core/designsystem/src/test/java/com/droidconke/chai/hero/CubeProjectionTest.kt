/*
 * Copyright 2022 DroidconKE
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

import org.junit.Assert.assertEquals
import org.junit.Test

class CubeProjectionTest {
    private val solved = CubeState.solved()

    @Test
    fun `the three-quarter pose shows nine stickers on each of three faces`() {
        val stickers = solved.visibleQuads(RestingPose.YAW, RestingPose.PITCH).filter { it.face != CubeFace.Inner }

        assertEquals(27, stickers.size)
        assertEquals(setOf(CubeFace.Front, CubeFace.Right, CubeFace.Up), stickers.map { it.face }.toSet())
    }

    @Test
    fun `faces are painted back to front`() {
        val depths = solved.visibleQuads(RestingPose.YAW, RestingPose.PITCH).map { it.depth }

        assertEquals(depths.sorted(), depths)
    }

    @Test
    fun `half way through a turn, the turning layer shows its side stickers and the rest do not`() {
        val head = solved.visibleQuads(0f, 0f).map { it.face }
        val turning = solved.visibleQuads(0f, 0f, Turn(CubeMove.U, 45f)).map { it.face }

        assertEquals(listOf(CubeFace.Front), head.filter { it != CubeFace.Inner }.distinct())
        assertEquals(3, turning.count { it == CubeFace.Right })
        assertEquals(9, turning.count { it == CubeFace.Front })
    }

    @Test
    fun `a full quarter turn drawn mid-animation matches the committed move`() {
        val animated = solved.visibleQuads(RestingPose.YAW, RestingPose.PITCH, Turn(CubeMove.R, 90f))
        val committed = solved.apply(CubeMove.R).visibleQuads(RestingPose.YAW, RestingPose.PITCH)

        assertEquals(committed.map { it.face }.sorted(), animated.map { it.face }.sorted())
    }
}