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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyMedium

/** Test tag, so a screenshot or semantics test can tell an empty pane from a missing one. */
const val DETAIL_PANE_PLACEHOLDER_TEST_TAG: String = "detail_pane_placeholder"

/**
 * What the detail pane shows before anything in the list has been picked.
 *
 * A two-pane layout has a state a phone never has: the detail exists and is empty. Leaving it
 * blank reads as a rendering bug, so it says what to do instead.
 */
@Composable
fun DetailPanePlaceholder(
    message: String,
    icon: Painter,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.testTag(DETAIL_PANE_PLACEHOLDER_TEST_TAG).fillMaxSize(),
        color = MaterialTheme.chaiColorsPalette.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                modifier = Modifier.size(56.dp),
                painter = icon,
                // Decorative: the message below carries the meaning.
                contentDescription = null,
                tint = MaterialTheme.chaiColorsPalette.textWeakColor,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ChaiBodyMedium(
                bodyText = message,
                textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}