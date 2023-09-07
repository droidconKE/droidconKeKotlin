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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.droidconke.chai.atoms.MontserratBold
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
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 20.dp)
            .testTag("organized_by_section")
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.organized_by),
            style = TextStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                fontFamily = MontserratBold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            organizationLogos.forEach { logo ->

                AsyncImage(
                    modifier = Modifier
                        .padding(6.dp),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(logo)
                        .build(),
                    placeholder = painterResource(R.drawable.ic_google_logo_icon),
                    contentDescription = stringResource(id = R.string.logo)
                )
            }
        }
    }
}