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

import com.droidconke.chai.colors.CubeBlueInk
import com.droidconke.chai.colors.CubeGreenInk
import com.droidconke.chai.colors.CubeWhiteInk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CubeMarksTest {
    private val outer = CubeFace.entries - CubeFace.Inner

    @Test
    fun `every outer face carries a mark, and the inside none`() {
        outer.forEach { assertNotNull(it.name, CubeMarks.of(it)) }
        assertNull(CubeMarks.of(CubeFace.Inner))
    }

    @Test
    fun `each mark sits inside its fit box, centred on the face`() {
        val margin = (CubeMark.FACE - CubeMark.FIT_WIDTH) / 2 - TOLERANCE
        outer.forEach { face ->
            CubeMarks.of(face)!!.layers.forEach { layer ->
                with(layer.bounds) {
                    assertTrue("$face $this", left >= margin && right <= CubeMark.FACE - margin)
                    assertTrue("$face $this", top >= margin && bottom <= CubeMark.FACE - margin)
                    assertTrue("$face $this", width > 0 && height > 0)
                }
            }
        }
    }

    @Test
    fun `each mark fills its fit box along one side`() {
        outer.forEach { face ->
            val layers = CubeMarks.of(face)!!.layers
            val width = layers.maxOf { it.bounds.right } - layers.minOf { it.bounds.left }
            val height = layers.maxOf { it.bounds.bottom } - layers.minOf { it.bounds.top }
            assertTrue("$face $width x $height", width > CubeMark.FIT_WIDTH * FILLED || height > CubeMark.FIT_HEIGHT * FILLED)
        }
    }

    @Test
    fun `the wordmarks keep both of their inks`() {
        assertEquals(listOf(CubeWhiteInk, CubeGreenInk), CubeMarks.of(CubeFace.Front)!!.layers.map { it.color })
        assertEquals(listOf(CubeWhiteInk, CubeBlueInk), CubeMarks.of(CubeFace.Right)!!.layers.map { it.color })
    }

    private companion object {
        const val TOLERANCE = 0.01f
        const val FILLED = 0.95f
    }
}