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
package com.android254.presentation.home.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyXSmallBold
import com.droidconke.chai.components.ChaiSubTitle
import com.droidconke.chai.components.ChaiTextLabelMedium
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeSectionHeaderComponent(
    sectionLabel: String,
    sectionSize: Int,
    onViewAllClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(vertical = 16.dp)
            .testTag("sectionHeader")
    ) {
        ChaiSubTitle(
            titleText = sectionLabel,
            titleColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onViewAllClicked() }
                .testTag("viewAll")
        ) {
            ChaiBodyXSmallBold(
                bodyText = stringResource(id = R.string.view_all_label),
                textColor = MaterialTheme.chaiColorsPalette.linkTextColorPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .height(22.dp)
                    .width(34.dp)
                    .background(
                        color = MaterialTheme.chaiColorsPalette.linkTextColorPrimary.copy(alpha = 0.11f),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                ChaiTextLabelMedium(
                    modifier = Modifier.align(Alignment.Center),
                    bodyText = stringResource(id = R.string.format_plus_label, sectionSize),
                    textColor = MaterialTheme.chaiColorsPalette.linkTextColorPrimary
                )
            }
        }
    }
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HomeSectionHeaderComponentPreview() {
    ChaiDCKE22Theme {
        HomeSectionHeaderComponent(
            sectionLabel = "Sessions",
            sectionSize = 20,
            onViewAllClicked = {}
        )
    }
}