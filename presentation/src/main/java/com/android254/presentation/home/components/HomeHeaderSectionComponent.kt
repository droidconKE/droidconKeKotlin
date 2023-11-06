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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyMediumBold
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeHeaderSectionComponent() {
    ChaiBodyMediumBold(
        modifier = Modifier.testTag("home_header"),
        bodyText = stringResource(id = R.string.home_header_welcome_label),
        textColor = MaterialTheme.chaiColorsPalette.textBoldColor
    )
}

@ChaiLightAndDarkComposePreview
@Composable
private fun HomeHeaderSectionComponentPreview() {
    ChaiDCKE22Theme {
        HomeHeaderSectionComponent()
    }
}