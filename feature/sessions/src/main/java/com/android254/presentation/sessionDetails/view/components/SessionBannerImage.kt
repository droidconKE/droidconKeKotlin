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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.navigation.sessionSharedImage
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.droidconke.chai.atoms.ChaiTeal90
import ke.droidcon.kotlin.core.ui.R

@Composable
fun SessionBannerImage(
    sessionDetails: SessionDetailsPresentationModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val model =
        remember(sessionDetails.sessionImageUrl) {
            ImageRequest
                .Builder(context)
                .data(sessionDetails.sessionImageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.all)
                .build()
        }
    AsyncImage(
        model = model,
        placeholder = painterResource(R.drawable.all),
        error = painterResource(R.drawable.all),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .sessionSharedImage(sessionDetails.id, RoundedCornerShape(10.dp))
                .border(1.dp, ChaiTeal90, RoundedCornerShape(10.dp))
                .testTag(TestTag.IMAGE_BANNER),
    )
}