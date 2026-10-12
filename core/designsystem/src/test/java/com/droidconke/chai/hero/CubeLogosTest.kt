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

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CubeLogosTest {
    private val tiles = (0..2).flatMap { row -> (0..2).map { column -> column to row } }

    @Test
    fun `every outer face carries a logo and the inside carries none`() {
        CubeFace.entries.forEach { face ->
            val pixels = tiles.sumOf { (column, row) -> CubeLogos.runs(face, column, row).sumOf { it.end - it.start } }

            assertEquals(face.name, face != CubeFace.Inner, pixels > 0)
        }
    }

    @Test
    fun `runs stay inside their sticker and each ink is painted in one go`() {
        CubeFace.entries.forEach { face ->
            tiles.forEach { (column, row) ->
                val runs = CubeLogos.runs(face, column, row)
                runs.forEach { run ->
                    assertTrue(run.row in 0 until CubeLogos.TILE)
                    assertTrue(run.start in 0 until run.end && run.end <= CubeLogos.TILE)
                }
                val groups = runs.map { it.color }.fold(emptyList<Color>()) { seen, colour -> if (seen.lastOrNull() == colour) seen else seen + colour }
                assertEquals("$face $column,$row", groups.distinct(), groups)
            }
        }
    }
}