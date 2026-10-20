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
package com.android254.presentation.sessionDetails.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.droidconke.chai.components.ChaiBodyMedium

@Composable
internal fun SessionDescription(
    description: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(description) { parseDescription(description) }
    if (blocks.isEmpty()) return
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) {
                val previous = blocks[index - 1]
                val tight = previous.text.endsWith(':') || (block is DescriptionBlock.Item && previous is DescriptionBlock.Item)
                Spacer(modifier = Modifier.height(if (tight) 4.dp else 12.dp))
            }
            when (block) {
                is DescriptionBlock.Paragraph -> ChaiBodyMedium(bodyText = block.text, textColor = color)
                is DescriptionBlock.Item ->
                    Row {
                        ChaiBodyMedium(bodyText = block.marker, textColor = color, modifier = Modifier.widthIn(min = 20.dp))
                        ChaiBodyMedium(bodyText = block.text, textColor = color, modifier = Modifier.weight(1f))
                    }
            }
        }
    }
}