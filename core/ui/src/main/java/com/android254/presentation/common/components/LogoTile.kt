/*
 * Copyright 2023 DroidconKE
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.collections.immutable.ImmutableList

/** A partner logo on a rounded tile. Tries each of [logos] in turn, then shows [name] rather than an empty tile. */
@Composable
fun LogoTile(
    logos: ImmutableList<String>,
    name: String,
    modifier: Modifier = Modifier,
    logoHeight: Dp = 44.dp,
) {
    var attempt by remember(logos) { mutableIntStateOf(0) }
    val logo = logos.getOrNull(attempt)
    val failed = logo == null
    Box(
        modifier =
            modifier
                .clip(MaterialTheme.shapes.largeIncreased)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (failed) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(logoHeight).fillMaxWidth().padding(top = 10.dp),
            )
        } else {
            AsyncImage(
                modifier = Modifier.widthIn(max = logoHeight * MAX_LOGO_ASPECT).fillMaxWidth().height(logoHeight),
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(logo)
                        .apply { if (logo.orEmpty().endsWith("svg")) decoderFactory(SvgDecoder.Factory()) }
                        .crossfade(true)
                        .build(),
                contentScale = ContentScale.Fit,
                contentDescription = name.ifBlank { null },
                onError = { attempt++ },
            )
        }
    }
}

/** Lays [items] out [columns] to a row with equal widths, so a short last row still lines up. */
@Composable
fun <T> EvenGrid(
    items: ImmutableList<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    spacing: Dp = 12.dp,
    rowSpacing: Dp = spacing,
    cell: @Composable (T, Modifier) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                row.forEach { item -> cell(item, Modifier.weight(1f)) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun <T> AdaptiveEvenGrid(
    items: ImmutableList<T>,
    minColumns: Int,
    minCellWidth: Dp,
    modifier: Modifier = Modifier,
    spacing: Dp = 12.dp,
    rowSpacing: Dp = spacing,
    cell: @Composable (T, Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = ((maxWidth + spacing) / (minCellWidth + spacing)).toInt().coerceAtLeast(minColumns)
        EvenGrid(items = items, columns = columns, spacing = spacing, rowSpacing = rowSpacing, cell = cell)
    }
}

private const val MAX_LOGO_ASPECT = 3f