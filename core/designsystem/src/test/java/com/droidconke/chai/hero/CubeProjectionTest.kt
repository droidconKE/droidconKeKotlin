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
import org.junit.Assert.assertTrue
import org.junit.Test

class CubeProjectionTest {
    private val solved = CubeState.solved()
    private val allTiles = (0..2).flatMap { row -> (0..2).map { column -> column to row } }.toSet()

    @Test
    fun `the three-quarter pose shows nine stickers on each of three faces`() {
        val stickers = solved.visibleQuads(RestingPose.YAW, RestingPose.PITCH).filter { it.face != CubeFace.Inner }

        assertEquals(mapOf(CubeFace.Front to 9, CubeFace.Right to 9, CubeFace.Up to 9), stickers.groupingBy { it.face }.eachCount())
    }

    @Test
    fun `seen head on, every face shows its whole logo the right way round`() {
        val headOn =
            mapOf(
                CubeFace.Front to (0f to 0f),
                CubeFace.Right to (-90f to 0f),
                CubeFace.Back to (180f to 0f),
                CubeFace.Left to (90f to 0f),
                CubeFace.Up to (0f to 90f),
                CubeFace.Down to (0f to -90f),
            )
        headOn.forEach { (face, pose) ->
            val stickers = solved.visibleQuads(pose.first, pose.second).filter { it.face == face }

            assertEquals(face.name, allTiles, stickers.map { it.tileColumn to it.tileRow }.toSet())
            stickers.forEach { quad ->
                assertTrue("$face reads left to right", quad.xs[1] > quad.xs[0])
                assertTrue("$face reads top to bottom", quad.ys[3] > quad.ys[0])
            }
            val byColumn = stickers.sortedBy { it.xs.average() }.map { it.tileColumn }
            val byRow = stickers.sortedBy { it.ys.average() }.map { it.tileRow }
            assertEquals("$face columns run left to right", byColumn.sorted(), byColumn)
            assertEquals("$face rows run top to bottom", byRow.sorted(), byRow)
        }
    }

    @Test
    fun `a turn carries logo tiles onto the next face`() {
        val stickers = solved.apply(CubeMove.R).visibleQuads(RestingPose.YAW, RestingPose.PITCH).filter { it.face != CubeFace.Inner }

        assertEquals(
            mapOf(CubeFace.Front to 9, CubeFace.Right to 9, CubeFace.Up to 6, CubeFace.Down to 3),
            stickers.groupingBy { it.face }.eachCount(),
        )
        assertEquals(allTiles, stickers.filter { it.face == CubeFace.Front }.map { it.tileColumn to it.tileRow }.toSet())
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
    fun `a turning layer facing the viewer is painted over the rest of the cube`() {
        val quads = solved.visibleQuads(RestingPose.YAW, RestingPose.PITCH, Turn(CubeMove.R, 45f))
        val lastStill = quads.indexOfLast { it.face == CubeFace.Front && it.tileColumn < 2 }
        val firstTurning = quads.indexOfFirst { it.face == CubeFace.Right }

        assertTrue(lastStill < firstTurning)
    }

    @Test
    fun `a turning layer facing away is painted before the rest of the cube`() {
        val quads = solved.visibleQuads(RestingPose.YAW, RestingPose.PITCH, Turn(CubeMove.L, 45f))
        val lastTurning = quads.indexOfLast { it.face == CubeFace.Front && it.tileColumn == 0 }
        val firstStill = quads.indexOfFirst { it.face == CubeFace.Front && it.tileColumn > 0 }

        assertTrue(lastTurning < firstStill)
    }

    @Test
    fun `at rest the hidden faces between cubelets are not drawn`() {
        val faces = solved.apply(SignatureSolve).visibleQuads(RestingPose.YAW, RestingPose.PITCH).map { it.face }

        assertEquals(27, faces.size)
        assertTrue(CubeFace.Inner !in faces)
    }

    @Test
    fun `a full quarter turn drawn mid-animation matches the committed move`() {
        val animated = solved.visibleQuads(RestingPose.YAW, RestingPose.PITCH, Turn(CubeMove.R, 90f))
        val committed = solved.apply(CubeMove.R).visibleQuads(RestingPose.YAW, RestingPose.PITCH)

        assertEquals(committed.map { it.face }.sorted(), animated.map { it.face }.filter { it != CubeFace.Inner }.sorted())
    }
}