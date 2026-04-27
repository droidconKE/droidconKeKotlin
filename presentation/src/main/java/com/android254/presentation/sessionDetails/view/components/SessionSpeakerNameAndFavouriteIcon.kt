package com.android254.presentation.sessionDetails.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiTextLabelLarge
import com.droidconke.chai.components.ChaiTitle
import ke.droidcon.kotlin.presentation.R

@Composable
fun SessionSpeakerNameAndFavouriteIcon(
    sessionDetails: SessionDetailsPresentationModel,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Android,
            contentDescription = null,
            modifier =
                Modifier
                    .height(14.dp)
                    .width(15.dp),
            tint = ChaiRed,
        )

        Spacer(modifier = Modifier.width(6.dp))

        ChaiTextLabelLarge(
            bodyText = stringResource(id = R.string.speaker_label),
            textColor = ChaiRed,
        )
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ChaiTitle(
            modifier = Modifier.testTag(TestTag.SPEAKER_NAME),
            titleText = sessionDetails.speakers.joinToString(" & ") { it.name },
            titleColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor,
        )

        IconButton(
            modifier = Modifier.size(32.dp),
            onClick = {
                if (sessionDetails.isStarred) {
                    unBookmarkSession(sessionDetails.id)
                } else {
                    bookmarkSession(sessionDetails.id)
                }
            },
        ) {
            Icon(
                modifier =
                    Modifier
                        .testTag(TestTag.FAVOURITE_ICON),
                imageVector = if (sessionDetails.isStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = stringResource(R.string.star_session_icon_description),
                tint = if (sessionDetails.isStarred) ChaiRed else MaterialTheme.chaiColorsPalette.secondaryButtonColor,
            )
        }
    }
}