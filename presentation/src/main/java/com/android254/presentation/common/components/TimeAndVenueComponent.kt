/*
 * Copyright 2022 DroidconKE
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.SessionPresentationModel
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyXSmall

@Composable
fun TimeAndVenueComponent(
    session: SessionPresentationModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ChaiBodyXSmall(
            bodyText = "${session.startTime} - ${session.endTime}",
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
            maxLines = 1,
        )
        ChaiBodyXSmall(
            bodyText = session.venue.uppercase(),
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
            maxLines = 2,
        )
    }
}