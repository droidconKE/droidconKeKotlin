package com.android254.presentation.sessionDetails.view.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyXSmall

@Composable
fun SessionTimeAndRoom(
    sessionDetails: SessionDetailsPresentationModel,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ChaiBodyXSmall(
            modifier = Modifier.testTag(TestTag.TIME_SLOT),
            bodyText = sessionDetails.timeSlot.uppercase(),
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
        )
        Spacer(modifier = Modifier.width(16.dp))
        ChaiBodyXSmall(
            bodyText = "|",
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
        )
        Spacer(modifier = Modifier.width(16.dp))
        ChaiBodyXSmall(
            modifier = Modifier.testTag(TestTag.ROOM),
            bodyText = sessionDetails.venue.uppercase(),
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
        )
    }
}