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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

private const val OPAQUE_ALPHA_THRESHOLD = 32
private const val LIGHT_LUMINANCE_THRESHOLD = 0.5f
private const val SAMPLE_SIZE = 20

/**
 * Renders [this] into a small offscreen sample and checks whether its opaque content reads as
 * visually light or dark, ignoring (near-)transparent padding. Null when the drawable has no
 * opaque content to judge. Temporarily overrides and restores the drawable's bounds; safe to
 * call from the main thread only, since drawables aren't thread-safe.
 */
internal fun Drawable.isPerceivedLight(): Boolean? {
    val originalBounds = copyBounds()
    val sample = Bitmap.createBitmap(SAMPLE_SIZE, SAMPLE_SIZE, Bitmap.Config.ARGB_8888)
    setBounds(0, 0, SAMPLE_SIZE, SAMPLE_SIZE)
    draw(Canvas(sample))
    bounds = originalBounds
    val pixels = IntArray(SAMPLE_SIZE * SAMPLE_SIZE)
    sample.getPixels(pixels, 0, SAMPLE_SIZE, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE)
    sample.recycle()
    return isPerceivedLight(pixels)
}

internal fun isPerceivedLight(pixels: IntArray): Boolean? {
    var luminanceTotal = 0.0
    var opaqueCount = 0
    for (pixel in pixels) {
        val alpha = (pixel ushr 24) and 0xFF
        if (alpha < OPAQUE_ALPHA_THRESHOLD) continue
        val r = (pixel ushr 16) and 0xFF
        val g = (pixel ushr 8) and 0xFF
        val b = pixel and 0xFF
        luminanceTotal += 0.299 * r + 0.587 * g + 0.114 * b
        opaqueCount++
    }
    if (opaqueCount == 0) return null
    val averageLuminance = (luminanceTotal / opaqueCount) / 255.0
    return averageLuminance >= LIGHT_LUMINANCE_THRESHOLD
}