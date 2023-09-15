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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android254.presentation.common.components.LoadingBox
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.atoms.MontserratBold
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeSpeakersLoadingComponent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.speakers_label),
                textAlign = TextAlign.Start,
                style = TextStyle(
                    color = ChaiBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 25.sp,
                    fontFamily = MontserratBold
                )
            )
            LoadingBox(height = 20.dp, width = 80.dp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            repeat(4) {
                HomeSpeakersLoadingItem()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeSpeakersLoadingComponentPreview() {
    DroidconKE2023Theme {
        HomeSpeakersLoadingComponent()
    }
}