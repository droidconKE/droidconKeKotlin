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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.navigation.sessionSharedTitle
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMedium

@Composable
fun SessionTitleAndDescription(
    sessionDetails: SessionDetailsPresentationModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ChaiBodyLargeBold(
            modifier =
                Modifier
                    .testTag(TestTag.SESSION_TITLE)
                    .sessionSharedTitle(sessionDetails.id),
            bodyText = sessionDetails.title,
            textColor = MaterialTheme.chaiColorsPalette.textNormalColor,
        )

        Spacer(modifier = Modifier.height(15.dp))

        ChaiBodyMedium(
            modifier = Modifier.testTag(TestTag.SESSION_DESCRIPTION),
            bodyText = sessionDetails.description,
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
        )
    }
}