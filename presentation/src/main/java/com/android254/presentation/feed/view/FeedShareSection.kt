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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android254.droidconKE2023.presentation.R
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiSmokeyGrey
import com.droidconke.chai.atoms.ChaiTeal
import com.droidconke.chai.atoms.MontserratBold

@Composable
fun FeedShareSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 48.dp)
            .testTag("share_bottom_sheet")
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TextButton(
                onClick = {
                }
            ) {
                Text(
                    text = stringResource(id = R.string.share),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 25.sp,
                    fontFamily = MontserratBold,
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = stringResource(id = R.string.share),
                    modifier = Modifier.padding(start = 8.dp),
                    tint = Color.Black
                )
            }

            Text(
                text = stringResource(id = R.string.cancel).uppercase(),
                color = ChaiSmokeyGrey,
                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                fontWeight = MaterialTheme.typography.labelLarge.fontWeight,
                fontStyle = MaterialTheme.typography.labelLarge.fontStyle
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
        border = BorderStroke(1.dp, ChaiTeal),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        )
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = R.string.share),
            tint = Color.Black
        )
        Text(
            text = platform,
            color = Color.Black,
            modifier = Modifier
                .padding(start = 24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlatformButtonPreview() {
    ChaiDCKE22Theme {
        PlatformButton("Twitter", R.drawable.ic_whatsapp)
    }
}

@Preview(showBackground = true)
@Composable
fun PFeedShareSectionPreview() {
    ChaiDCKE22Theme {
        FeedShareSection()
    }
}
