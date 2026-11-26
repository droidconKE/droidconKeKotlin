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

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SelectedCardBorderWidth = 2.dp
private const val SELECTED_CARD_CONTAINER_ALPHA = 0.22f

/** Shared active-item border for cards shown beside a detail pane in two-pane layouts. */
@Composable
fun selectedCardBorder(): BorderStroke = BorderStroke(width = SelectedCardBorderWidth, color = MaterialTheme.colorScheme.primary)

/** Shared tonal fill for cards shown beside a detail pane in two-pane layouts. */
@Composable
fun selectedCardContainerColor(): Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = SELECTED_CARD_CONTAINER_ALPHA)