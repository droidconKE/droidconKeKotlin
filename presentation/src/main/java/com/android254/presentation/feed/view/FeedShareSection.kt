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
package com.android254.presentation.feed.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiTeal90
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiSubTitle
import com.droidconke.chai.components.ChaiTextButtonLight
import ke.droidcon.kotlin.presentation.R

@Composable
fun FeedShareSection(
    onCancelClicked: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.chaiColorsPalette.bottomSheetBackgroundColor)
            .padding(start = 20.dp, top = 36.dp, end = 16.dp, bottom = 48.dp)
            .testTag("share_bottom_sheet")
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = stringResource(id = R.string.share),
                    modifier = Modifier.padding(end = 12.dp),
                    tint = MaterialTheme.chaiColorsPalette.textNormalColor
                )
                ChaiSubTitle(
                    titleText = stringResource(id = R.string.share),
                    titleColor = MaterialTheme.chaiColorsPalette.textNormalColor
                )
            }

            ChaiTextButtonLight(
                modifier = Modifier.clickable {
                    onCancelClicked()
                },
                bodyText = stringResource(id = R.string.cancel),
                textColor = MaterialTheme.chaiColorsPalette.textNormalColor
            )
        }

        val platforms = mapOf(
            "Twitter" to R.drawable.ic_twitter,
            "Facebook" to R.drawable.ic_facebook,
            "WhatsApp" to R.drawable.ic_whatsapp,
            "Telegram" to R.drawable.ic_telegram
        ).toList()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            content = {
                items(platforms) { platform ->
                    PlatformButton(platform = platform.first, icon = platform.second)
                }
            },
            modifier = Modifier
                .padding(top = 16.dp)
        )
    }
}

@Composable
fun PlatformButton(platform: String, icon: Int) {
    OutlinedButton(
        onClick = { /*TODO*/ },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        border = BorderStroke(1.dp, ChaiTeal90),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        )
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = R.string.share),
            tint = MaterialTheme.chaiColorsPalette.outlinedButtonTextColor
        )
        ChaiBodyMedium(
            modifier = Modifier
                .padding(start = 22.dp),
            bodyText = platform,
            textColor = MaterialTheme.chaiColorsPalette.outlinedButtonTextColor

        )
    }
}

@ChaiLightAndDarkComposePreview
@Composable
fun PlatformButtonPreview() {
    ChaiDCKE22Theme {
        PlatformButton("Twitter", R.drawable.ic_whatsapp)
    }
}

@ChaiLightAndDarkComposePreview
@Composable
fun PFeedShareSectionPreview() {
    ChaiDCKE22Theme {
        FeedShareSection()
    }
}