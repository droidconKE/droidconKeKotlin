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

import androidx.compose.runtime.Composable
import com.android254.presentation.common.components.DroidconAppBar
import com.android254.presentation.common.components.DroidconAppBarWithFeedbackButton
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme

@Composable
fun HomeToolbarComponent(
    isSignedIn: Boolean,
    navigateToFeedbackScreen: () -> Unit = {},
    onActionClicked: () -> Unit = {}
) {
    if (isSignedIn) {
        DroidconAppBarWithFeedbackButton(
            onButtonClick = {
                navigateToFeedbackScreen()
            },
            userProfile = "https://media-exp1.licdn.com/dms/image/C4D03AQGn58utIO-x3w/profile-displayphoto-shrink_200_200/0/1637478114039?e=2147483647&v=beta&t=3kIon0YJQNHZojD3Dt5HVODJqHsKdf2YKP1SfWeROnI"
        )
    } else {
        DroidconAppBar(
            onActionClicked = onActionClicked
        )
    }
}

@ChaiLightAndDarkComposePreview
@Composable
private fun HomeToolbarComponentPreview() {
    ChaiDCKE22Theme {
        HomeToolbarComponent(
            isSignedIn = false,
            navigateToFeedbackScreen = {},
            onActionClicked = {}
        )
    }
}