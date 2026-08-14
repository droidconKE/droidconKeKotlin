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
package com.android254.presentation.sessions.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.stepper.Intensity
import com.android254.presentation.common.stepper.VerticalStep
import com.android254.presentation.common.stepper.verticalSteps
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.chaiColorsPalette

@Composable
fun SessionLoadingComponent() {
    val data =
        List(3) { index ->
            VerticalStep(
                id = index,
                color = ChaiBlue,
                icon = Icons.Default.CoPresent,
                intensity = Intensity.Low,
                data = Unit,
            )
        }
    LazyColumn {
        verticalSteps(
            items = data,
            spacing = 16.dp,
        ) {
            SessionsLoadingCard()
        }
    }
}

@PreviewLightDark
@Composable
private fun SessionLoadingPreview() {
    ChaiTheme {
        Surface(
            color = MaterialTheme.chaiColorsPalette.background,
        ) {
            SessionLoadingComponent()
        }
    }
}