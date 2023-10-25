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
package com.android254.presentation.feed.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.models.FeedUI
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.atoms.ChaiSmokeyGrey
import com.droidconke.chai.atoms.MontserratBold
import ke.droidcon.kotlin.presentation.R

@Composable
fun FeedComponent(
    modifier: Modifier,
    feedPresentationModel: FeedUI,
    onClickItem: (Int) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 2.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(5),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val textFromNetwork = stringResource(id = R.string.placeholder_long_text)

            Text(
                text = feedPresentationModel.body,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                textAlign = TextAlign.Start,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(feedPresentationModel.image)
                    .build(),
                contentDescription = stringResource(id = R.string.feed_image)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        onClickItem(1)
                    },
                    modifier = Modifier.testTag("share_button")
                ) {
                    Text(
                        text = stringResource(id = R.string.share),
                        color = ChaiBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 25.sp,
                        fontFamily = MontserratBold
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = stringResource(id = R.string.share),
                        modifier = Modifier.padding(start = 8.dp),
                        tint = ChaiBlue
                    )
                }

                Text(
                    text = "5 hours ago",
                    color = ChaiSmokeyGrey,
                    fontSize = MaterialTheme.typography.labelLarge.fontSize,
                    fontWeight = MaterialTheme.typography.labelLarge.fontWeight,
                    fontStyle = MaterialTheme.typography.labelLarge.fontStyle
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    ChaiDCKE22Theme {
        FeedComponent(
            modifier = Modifier,
            feedPresentationModel =
            FeedUI("Feed", "Feed feed", "test", "", "", ""),
            onClickItem = {}
        )
    }
}