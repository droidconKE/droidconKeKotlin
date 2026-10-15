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
package ke.droidcon.kotlin.screenshot

import androidx.compose.ui.geometry.Rect
import com.dropbox.differ.Color
import com.dropbox.differ.Image
import org.junit.Assert.assertEquals
import org.junit.Test

class CubeTolerantComparatorTest {
    private val cube = Rect(2f, 2f, 6f, 6f)

    @Test
    fun `a few levels of drift pass inside the cube and fail outside it`() {
        val base = flat(0.5f)
        val drifted =
            flat(0.5f).apply {
                shade(x = 3, y = 3, by = DRIFT)
                shade(x = 0, y = 0, by = DRIFT)
            }

        val result = CubeTolerantComparator(listOf(cube)).compare(base, drifted)

        assertEquals(1, result.pixelDifferences)
        assertEquals(64, result.pixelCount)
    }

    @Test
    fun `a real change inside the cube still fails`() {
        val base = flat(0.5f)
        val changed = flat(0.5f).apply { shade(x = 3, y = 3, by = CHANGE) }

        assertEquals(1, CubeTolerantComparator(listOf(cube)).compare(base, changed).pixelDifferences)
    }

    @Test
    fun `pixels only one image has count as differences`() {
        val wide = flat(0.5f, width = 10)

        assertEquals(16, CubeTolerantComparator(emptyList()).compare(flat(0.5f), wide).pixelDifferences)
    }

    private class Grid(
        override val width: Int,
        override val height: Int,
        private val pixels: Array<Color>,
    ) : Image {
        override fun getPixel(
            x: Int,
            y: Int,
        ) = pixels[y * width + x]

        fun shade(
            x: Int,
            y: Int,
            by: Float,
        ) {
            val c = pixels[y * width + x]
            pixels[y * width + x] = c.copy(r = c.r + by)
        }
    }

    private fun flat(
        grey: Float,
        width: Int = 8,
    ) = Grid(width, 8, Array(width * 8) { Color(grey, grey, grey) })

    private companion object {
        /** Six levels of 255 in one channel: under the cube's allowance, over the exact one. */
        const val DRIFT = 6f / 255f
        const val CHANGE = 0.2f
    }
}