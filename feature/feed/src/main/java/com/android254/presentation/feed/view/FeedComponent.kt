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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.android254.presentation.common.components.SessionTag
import com.android254.presentation.models.FeedUI
import com.droidconke.chai.ChaiTheme
import ke.droidcon.kotlin.core.ui.R

@Composable
fun FeedComponent(
    feed: FeedUI,
    modifier: Modifier = Modifier,
    onShare: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (feed.topic.isNotBlank()) {
                SessionTag(tagText = feed.topic)
            }
            Text(
                text = feed.createdAt,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (feed.title.isNotBlank()) {
            Text(
                text = feed.title,
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = feed.body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        feed.image?.let {
            AsyncImage(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(FEED_IMAGE_ASPECT_RATIO)
                        .clip(MaterialTheme.shapes.largeIncreased),
                contentScale = ContentScale.Crop,
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(feed.image)
                        .build(),
                contentDescription = stringResource(id = R.string.feed_image),
            )
        }

        FilledTonalButton(
            onClick = onShare,
            modifier = Modifier.testTag("share_button"),
            colors =
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_share),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.share), style = MaterialTheme.typography.labelLarge)
        }
    }
}

private const val FEED_IMAGE_ASPECT_RATIO = 16f / 9f

@Preview
@Composable
private fun Preview() {
    ChaiTheme {
        FeedComponent(
            modifier = Modifier,
            feed =
                FeedUI("Feed", "Feed feed", "test", "", "", ""),
        )
    }
}