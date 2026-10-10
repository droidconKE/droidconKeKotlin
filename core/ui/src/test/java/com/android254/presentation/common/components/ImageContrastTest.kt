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
package com.android254.presentation.common.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageContrastTest {
    private fun pixelsOf(vararg argb: Int) = argb

    private val opaqueBlack = 0xFF000000.toInt()
    private val opaqueWhite = 0xFFFFFFFF.toInt()
    private val transparent = 0x00000000

    @Test
    fun `solid opaque black is not perceived light`() {
        val pixels = IntArray(16) { opaqueBlack }
        assertEquals(false, isPerceivedLight(pixels))
    }

    @Test
    fun `solid opaque white is perceived light`() {
        val pixels = IntArray(16) { opaqueWhite }
        assertEquals(true, isPerceivedLight(pixels))
    }

    @Test
    fun `transparent padding around a dark mark is ignored`() {
        val pixels = IntArray(100) { transparent }
        // A handful of opaque black pixels in an otherwise fully transparent image.
        for (i in 0 until 10) pixels[i] = opaqueBlack
        assertEquals(false, isPerceivedLight(pixels))
    }

    @Test
    fun `transparent padding around a light mark is ignored`() {
        val pixels = IntArray(100) { transparent }
        for (i in 0 until 10) pixels[i] = opaqueWhite
        assertEquals(true, isPerceivedLight(pixels))
    }

    @Test
    fun `fully transparent image has no verdict`() {
        val pixels = pixelsOf(transparent, transparent, transparent)
        assertNull(isPerceivedLight(pixels))
    }

    @Test
    fun `near-transparent pixels below the alpha threshold are ignored`() {
        val almostInvisibleWhite = 0x05FFFFFF
        val pixels = IntArray(50) { almostInvisibleWhite }
        for (i in 0 until 5) pixels[i] = opaqueBlack
        assertEquals(false, isPerceivedLight(pixels))
    }
}
