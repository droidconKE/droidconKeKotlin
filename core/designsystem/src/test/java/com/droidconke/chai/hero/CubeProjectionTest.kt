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
import kotlin.math.abs
import kotlin.math.cos

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
    fun `each showcase pose turns its face to the viewer, whole and upright`() {
        Showcase.forEach { face ->
            val quads = solved.visibleQuads(presenting(face))
            val shown = quads.filter { it.face == face }
            val brightest = quads.groupBy { it.face }.maxBy { (_, faceQuads) -> faceQuads.sumOf { it.light.toDouble() } }.key

            assertEquals(face.name, face, brightest)
            assertEquals(face.name, allTiles, shown.map { it.tileColumn to it.tileRow }.toSet())
            shown.forEach { quad ->
                assertTrue("$face reads left to right", quad.xs[1] > quad.xs[0])
                assertTrue("$face reads top to bottom", quad.ys[3] > quad.ys[0])
            }
        }
    }

    @Test
    fun `the cube stays within its reach at rest, leaning, and in every pose it travels through`() {
        val resting = Poses.maxOf { reach(it) }
        var moving = 0f
        Poses.forEachIndexed { i, pose ->
            (0..STEPS).forEach { step ->
                val lean = (1 - cos(2 * Math.PI.toFloat() * step / STEPS)) / 2
                moving = maxOf(moving, reach(pose + Orientation.euler(LEAN_YAW * lean, LEAN_PITCH * lean)))
            }
            val next = (i + 1) % Showcase.size
            (0..STEPS).forEach { step ->
                val travelling = Travels[i].at(step / STEPS.toFloat())
                moving = maxOf(moving, reach(travelling))
                scrambleTowards(Showcase[next]).forEach { move ->
                    (0..90 step 15).forEach { degrees -> moving = maxOf(moving, reach(travelling, Turn(move, degrees.toFloat()))) }
                }
            }
        }

        assertEquals(RESTING_REACH, resting, REACH_SLACK)
        assertTrue("moving reach $moving", moving <= MOVING_REACH)
    }

    private fun reach(
        orientation: Orientation,
        turn: Turn? = null,
    ) = solved.visibleQuads(orientation, turn).maxOf { quad -> maxOf(quad.xs.maxOf(::abs), quad.ys.maxOf(::abs)) }

    @Test
    fun `the front face's pose is the resting view`() {
        val presented = solved.visibleQuads(presenting(CubeFace.Front))
        val resting = solved.visibleQuads(RestingPose.YAW, RestingPose.PITCH, rollDegrees = RestingPose.ROLL)

        presented.zip(resting).forEach { (a, b) ->
            assertEquals(b.face, a.face)
            (0 until 4).forEach { i -> assertEquals(b.xs[i], a.xs[i], TOLERANCE) }
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
    fun `while a layer turns, only the faces along the cut are drawn between cubelets`() {
        Showcase.forEach { face ->
            scrambleTowards(face).forEach { move ->
                (15..75 step 30).forEach { degrees ->
                    val quads = solved.visibleQuads(RestingPose.YAW, RestingPose.PITCH, Turn(move, degrees.toFloat()))
                    val inner = quads.count { it.face == CubeFace.Inner }

                    assertTrue("$move at $degrees: $inner inner faces", inner in 1..9)
                    assertTrue("$move at $degrees: ${quads.size} quads", quads.size <= MOST_QUADS)
                }
            }
        }
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
    fun `a layer whose axis barely leans to the viewer is still behind the cut, so it is painted first`() {
        // Yawed 1.5 degrees, the right layer's axis leans towards the camera by sin 1.5, under the
        // half cubelet over nine that puts the camera on its side of the cut. At 5 degrees it is over.
        val barely = solved.visibleQuads(-1.5f, 0f, Turn(CubeMove.R, 45f))
        val clearly = solved.visibleQuads(-5f, 0f, Turn(CubeMove.R, 45f))

        assertTrue(barely.lastTurningFront() < barely.firstStillFront())
        assertTrue(clearly.firstTurningFront() > clearly.lastStillFront())
    }

    private fun List<Quad>.lastTurningFront() = indexOfLast { it.face == CubeFace.Front && it.tileColumn == 2 }

    private fun List<Quad>.firstTurningFront() = indexOfFirst { it.face == CubeFace.Front && it.tileColumn == 2 }

    private fun List<Quad>.firstStillFront() = indexOfFirst { it.face == CubeFace.Front && it.tileColumn < 2 }

    private fun List<Quad>.lastStillFront() = indexOfLast { it.face == CubeFace.Front && it.tileColumn < 2 }

    @Test
    fun `at rest the hidden faces between cubelets are not drawn`() {
        val faces = solved.apply(scrambleTowards(CubeFace.Up)).visibleQuads(RestingPose.YAW, RestingPose.PITCH).map { it.face }

        assertEquals(27, faces.size)
        assertTrue(CubeFace.Inner !in faces)
    }

    @Test
    fun `a full quarter turn drawn mid-animation matches the committed move`() {
        val animated = solved.visibleQuads(RestingPose.YAW, RestingPose.PITCH, Turn(CubeMove.R, 90f))
        val committed = solved.apply(CubeMove.R).visibleQuads(RestingPose.YAW, RestingPose.PITCH)

        assertEquals(committed.map { it.face }.sorted(), animated.map { it.face }.filter { it != CubeFace.Inner }.sorted())
    }

    private companion object {
        const val TOLERANCE = 1e-4f
        const val REACH_SLACK = 0.01f
        const val STEPS = 20

        /** Twenty-seven stickers, nine sides the turning layer bares, nine faces along the cut. */
        const val MOST_QUADS = 45
    }
}