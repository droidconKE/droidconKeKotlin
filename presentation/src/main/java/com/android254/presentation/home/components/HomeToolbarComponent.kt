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