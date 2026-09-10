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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodySmall

@Composable
fun SessionLevel(
    sessionLevel: String,
    modifier: Modifier = Modifier,
) {
    ChaiBodySmall(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.chaiColorsPalette.badgeBackgroundColor,
                    shape = RoundedCornerShape(5.dp),
                ).padding(vertical = 3.dp, horizontal = 9.dp)
                .testTag(TestTag.LEVEL),
        bodyText = "#$sessionLevel".uppercase(),
        textColor = ChaiWhite,
    )
}