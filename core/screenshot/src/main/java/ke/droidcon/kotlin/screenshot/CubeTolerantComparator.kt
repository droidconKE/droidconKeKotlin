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
import com.dropbox.differ.Image
import com.dropbox.differ.ImageComparator
import com.dropbox.differ.Mask
import com.dropbox.differ.distance

/** Roborazzi's comparison, but looser inside [cube], whose path edges anti-alias differently on Linux CI. */
internal class CubeTolerantComparator(
    private val cube: List<Rect>,
) : ImageComparator {
    override fun compare(
        left: Image,
        right: Image,
        diff: Mask?,
    ): ImageComparator.ComparisonResult {
        val width = maxOf(left.width, right.width)
        val height = maxOf(left.height, right.height)
        val sharedWidth = minOf(left.width, right.width)
        val sharedHeight = minOf(left.height, right.height)
        var misses = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val differs = x >= sharedWidth || y >= sharedHeight || differs(left, right, x, y)
                if (differs) {
                    misses++
                    diff?.setValue(x, y, 1f)
                }
            }
        }
        return ImageComparator.ComparisonResult(misses, width * height, width, height)
    }

    private fun differs(
        left: Image,
        right: Image,
        x: Int,
        y: Int,
    ): Boolean {
        val allowed = if (cube.any { it.contains(x, y) }) PATH_EDGE_DISTANCE else EXACT_DISTANCE
        return left.getPixel(x, y).distance(right.getPixel(x, y)) > allowed
    }

    private fun Rect.contains(
        x: Int,
        y: Int,
    ) = x >= left && x < right && y >= top && y < bottom

    companion object {
        /** Roborazzi's default, which has held every golden so far. */
        const val EXACT_DISTANCE = 0.007f

        /** About eight levels in one channel: what a path edge drifts by between platforms. */
        const val PATH_EDGE_DISTANCE = 0.03f
    }
}