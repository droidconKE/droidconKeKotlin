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

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.MontserratSemiBold
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeHeaderSectionComponent() {
    Text(
        text = stringResource(id = R.string.home_header_welcome_label),
        modifier = Modifier.testTag("home_header"),
        fontFamily = MontserratSemiBold,
        fontSize = 16.sp
    )
}

@ChaiLightAndDarkComposePreview
@Composable
private fun HomeHeaderSectionComponentPreview() {
    ChaiDCKE22Theme {
        HomeHeaderSectionComponent()
    }
}