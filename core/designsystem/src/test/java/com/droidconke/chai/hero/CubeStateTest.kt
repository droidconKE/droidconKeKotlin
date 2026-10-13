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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CubeStateTest {
    private val solved = CubeState.solved()
    private val faces = with(CubeMove) { listOf(R, L, U, D, F, B) }

    @Test
    fun `a solved cube has 27 cubelets and is solved`() {
        assertEquals(27, solved.cubelets.size)
        assertTrue(solved.isSolved)
    }

    @Test
    fun `any face turned four times is back where it started`() {
        faces.forEach { move -> assertEquals(move.toString(), solved, solved.apply(List(4) { move })) }
    }

    @Test
    fun `a single turn unsolves the cube and moves exactly one layer of nine`() {
        faces.forEach { move ->
            val turned = solved.apply(move)
            assertFalse(turned.isSolved)
            assertEquals(9, turned.cubelets.zip(solved.cubelets).count { (a, b) -> a != b })
        }
    }

    @Test
    fun `a move and its inverse cancel`() {
        faces.forEach { move -> assertEquals(solved, solved.apply(move).apply(move.inverse())) }
    }

    @Test
    fun `each scene's scramble unsolves the cube, and its reverse solves it`() {
        Showcase.forEach { face ->
            val scramble = scrambleTowards(face)
            val scrambled = solved.apply(scramble)

            assertFalse(face.name, scrambled.isSolved)
            assertTrue(face.name, scrambled.apply(scramble.inverse()).isSolved)
        }
    }

    @Test
    fun `inverting a sequence reverses its order and inverts each move`() {
        val sequence = with(CubeMove) { listOf(R, U, F.inverse()) }

        assertEquals(with(CubeMove) { listOf(F, U.inverse(), R.inverse()) }, sequence.inverse())
    }

    @Test
    fun `clockwise is seen from outside the face`() {
        // R clockwise from the right carries the front-top edge to the back-top.
        val frontTop = Vec3(1, 1, 1)

        assertEquals(Vec3(1, 1, -1), frontTop.quarterTurn(Axis.X, CubeMove.R.sign))
    }
}