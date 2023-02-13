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
package com.android254.presentation.speakers.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android254.droidconKE2023.presentation.R
import com.android254.presentation.common.theme.DroidconKE2022Theme

@Composable
fun TopAppBar(modifier: Modifier = Modifier, onBackPressed: () -> Unit = {}) {
    Box(
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.topbar_speaker_bg),
            contentDescription = stringResource(R.string.login_screen_bg_image_description),
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = stringResource(R.string.back_arrow_icon_description),
                    tint = colorResource(id = R.color.smoke_white)
                )
            }

            Text(
                text = stringResource(id = R.string.speaker_details_label),
                fontSize = 18.sp,
                lineHeight = 22.sp,
                color = colorResource(id = R.color.smoke_white),
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}

@Composable
@Preview
fun TopAppBarPreview() {
    DroidconKE2022Theme {
        TopAppBar()
    }
}