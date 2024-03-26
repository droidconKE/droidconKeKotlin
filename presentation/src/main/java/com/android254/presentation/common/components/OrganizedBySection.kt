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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiTitle
import ke.droidcon.kotlin.presentation.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizedBySection(
    modifier: Modifier = Modifier,
    organizationLogos: List<String>
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.chaiColorsPalette.surfaces,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 20.dp)
            .testTag("organized_by_section"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChaiTitle(
            modifier = Modifier
                .padding(start = 20.dp),
            titleText = stringResource(id = R.string.organized_by),
            titleColor = MaterialTheme.chaiColorsPalette.textLabelAndHeadings

        )

        Spacer(modifier = Modifier.height(40.dp))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 3
        ) {
            organizationLogos.forEach { logo ->

                AsyncImage(
                    modifier = Modifier
                        .height(80.dp)
                        .padding(6.dp),
                    model = if (logo.endsWith("svg")) {
                        ImageRequest.Builder(LocalContext.current)
                            .data(logo)
                            .decoderFactory(SvgDecoder.Factory())
                            .build()
                    } else {
                        ImageRequest.Builder(LocalContext.current)
                            .data(logo)
                            .build()
                    },
                    placeholder = painterResource(R.drawable.ic_google_logo_icon),
                    contentDescription = stringResource(id = R.string.logo)
                )
            }
        }
    }
}