package com.android254.presentation.sessionDetails.view.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.droidconke.chai.atoms.ChaiTeal90

@Composable
fun SessionBannerImage(sessionDetails: SessionDetailsPresentationModel) {
    AsyncImage(
        model = sessionDetails.sessionImageUrl,
        contentDescription = null,
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, ChaiTeal90, RoundedCornerShape(10.dp))
                .testTag(TestTag.IMAGE_BANNER)
                .clip(RoundedCornerShape(10.dp)),
    )
}