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
private fun HomeHeaderSectionComponentPreview(){
    ChaiDCKE22Theme {
        HomeHeaderSectionComponent()
    }
}