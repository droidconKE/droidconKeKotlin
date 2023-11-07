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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.models.FeedUI
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiBodySmallBold
import com.droidconke.chai.components.ChaiBodyXSmall
import ke.droidcon.kotlin.presentation.R

@Composable
fun FeedComponent(
    modifier: Modifier,
    feed: FeedUI,
    onClickItem: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .background(color = MaterialTheme.chaiColorsPalette.surfaces)
            .padding(vertical = (0.5).dp)
            .background(color = MaterialTheme.chaiColorsPalette.background)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChaiBodyMedium(
            bodyText = feed.body,
            textColor = MaterialTheme.chaiColorsPalette.textNormalColor
        )

        feed.image?.let {
            AsyncImage(
                modifier = Modifier.fillMaxWidth()
                    .height(209.dp)
                    .clip(shape = RoundedCornerShape(8.dp)),
                contentScale = ContentScale.FillHeight,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(feed.image)
                    .build(),
                contentDescription = stringResource(id = R.string.feed_image)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    onClickItem(1)
                },
                modifier = Modifier.testTag("share_button")
                    .offset(x = (-12).dp)
            ) {
                ChaiBodySmallBold(
                    bodyText = stringResource(id = R.string.share),
                    textColor = MaterialTheme.chaiColorsPalette.textButtonColor
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = stringResource(id = R.string.share),
                    modifier = Modifier.padding(start = 8.dp),
                    tint = MaterialTheme.chaiColorsPalette.textButtonColor
                )
            }

            ChaiBodyXSmall(
                bodyText = feed.createdAt,
                textColor = MaterialTheme.chaiColorsPalette.textWeakColor
            )
        }
    }
}

@Preview
@Composable
fun Preview() {
    ChaiDCKE22Theme {
        FeedComponent(
            modifier = Modifier,
            feed =
            FeedUI("Feed", "Feed feed", "test", "", "", ""),
            onClickItem = {}
        )
    }
}