package com.android254.presentation.sessionDetails.view.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMedium

@Composable
fun SessionTitleAndDescription(
    sessionDetails: SessionDetailsPresentationModel,
) {
    ChaiBodyLargeBold(
        modifier = Modifier.testTag(TestTag.SESSION_TITLE),
        bodyText = sessionDetails.title,
        textColor = MaterialTheme.chaiColorsPalette.textNormalColor,
    )

    Spacer(modifier = Modifier.height(15.dp))

    ChaiBodyMedium(
        modifier = Modifier.testTag(TestTag.SESSION_DESCRIPTION),
        bodyText = sessionDetails.description,
        textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
    )
}