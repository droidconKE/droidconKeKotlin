/*
 * Copyright 2022 DroidconKE
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
package com.android254.presentation.sessionDetails.view

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.AlertDialogDefaults.titleContentColor
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.SessionDetailsViewModel
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.atoms.ChaiTeal90
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.COutlinedButton
import com.droidconke.chai.components.ChaiBodyLarge
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiBodySmall
import com.droidconke.chai.components.ChaiBodyXSmall
import com.droidconke.chai.components.ChaiTextLabelLarge
import com.droidconke.chai.components.ChaiTitle
import ke.droidcon.kotlin.presentation.R

@Composable
fun SessionDetailsRoute(
    viewModel: SessionDetailsViewModel = hiltViewModel(),
    sessionId: String,
    onNavigationIconClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SessionDetailsScreen(
        uiState = uiState,
        sessionId = sessionId,
        bookmarkSession = viewModel::bookmarkSession,
        unBookmarkSession = viewModel::unBookmarkSession,
        onNavigationIconClick = onNavigationIconClick
    )
}

@Composable
private fun SessionDetailsScreen(
    uiState: SessionDetailsUiState,
    sessionId: String,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
    onNavigationIconClick: () -> Unit
) {
    Scaffold(
        topBar = { TopBar(onNavigationIconClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                modifier = Modifier
                    .size(44.dp)
                    .testTag(TestTag.FLOATING_ACTION_BUTTON),
                containerColor = ChaiRed,
                shape = CircleShape
            ) {
                Icon(
                    modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                    imageVector = Icons.Filled.Reply,
                    contentDescription = null,
                    tint = ChaiWhite
                )
            }
        },
        containerColor = MaterialTheme.chaiColorsPalette.background
    ) { paddingValues ->
        when (uiState) {
            is SessionDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            is SessionDetailsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ChaiBodyMediumBold(
                        modifier = Modifier.align(Alignment.Center),
                        bodyText = uiState.message,
                        textColor = MaterialTheme.chaiColorsPalette.textNormalColor
                    )
                }
            }

            is SessionDetailsUiState.Success -> {
                Body(
                    paddingValues = paddingValues,
                    sessionDetails = uiState.data,
                    bookmarkSession = bookmarkSession,
                    unBookmarkSession = unBookmarkSession
                )
            }
        }
    }
}

@Composable
fun Body(
    paddingValues: PaddingValues,
    sessionDetails: SessionDetailsPresentationModel,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomDivider()
        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            SessionSpeakerNameAndFavouriteIcon(
                sessionDetails = sessionDetails,
                bookmarkSession = bookmarkSession,
                unBookmarkSession = unBookmarkSession
            )

            Spacer(modifier = Modifier.height(25.dp))

            SessionTitleAndDescription(sessionDetails)

            Spacer(modifier = Modifier.height(15.dp))

            SessionBannerImage(sessionDetails)
        }

        Spacer(modifier = Modifier.height(25.dp))

        CustomDivider()

        Spacer(modifier = Modifier.height(19.dp))

        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp)) {
            SessionTimeAndRoom(sessionDetails)

            Spacer(modifier = Modifier.height(15.dp))

            SessionLevel(sessionDetails.level)

            Spacer(modifier = Modifier.height(18.dp))
        }

        CustomDivider()

        Spacer(modifier = Modifier.height(18.dp))

        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp)) {
            sessionDetails.speakers.forEach { speaker ->
                if (speaker.twitterHandle.isNotEmpty()) {
                    SpeakerTwitterHandle(speaker)
                }
            }
        }

        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
private fun SpeakerTwitterHandle(
    speaker: SessionDetailsSpeakerPresentationModel
) {
    val context = LocalContext.current
    val intent = remember {
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.twitter.com/${speaker.twitterHandle}")
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ChaiBodyMedium(
            bodyText = stringResource(R.string.twitter_handle_label),
            textColor = MaterialTheme.chaiColorsPalette.textNormalColor
        )

        COutlinedButton(
            onClick = { context.startActivity(intent) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.chaiColorsPalette.outlinedButtonBackgroundColor
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_twitter_logo),
                contentDescription = null,
                modifier = Modifier
                    .height(20.dp)
                    .width(20.dp),
                tint = MaterialTheme.chaiColorsPalette.secondaryButtonColor
            )

            Spacer(modifier = Modifier.width(5.dp))

            ChaiBodyMedium(
                modifier = Modifier.testTag(TestTag.TWITTER_HANDLE_TEXT),
                bodyText = speaker.twitterHandle,
                textColor = MaterialTheme.chaiColorsPalette.secondaryButtonColor
            )
        }
    }
}

@Composable
private fun SessionBannerImage(sessionDetails: SessionDetailsPresentationModel) {
    AsyncImage(
        model = sessionDetails.sessionImageUrl,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, ChaiTeal90, RoundedCornerShape(10.dp))
            .testTag(TestTag.IMAGE_BANNER)
            .clip(RoundedCornerShape(10.dp))
    )
}

@Composable
private fun SessionSpeakerNameAndFavouriteIcon(
    sessionDetails: SessionDetailsPresentationModel,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Android,
            contentDescription = null,
            modifier = Modifier
                .height(14.dp)
                .width(15.dp),
            tint = ChaiRed
        )

        Spacer(modifier = Modifier.width(6.dp))

        ChaiTextLabelLarge(
            bodyText = stringResource(id = R.string.speaker_label),
            textColor = ChaiRed
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ChaiTitle(
            modifier = Modifier.testTag(TestTag.SPEAKER_NAME),
            titleText = sessionDetails.speakers.joinToString(" & ") { it.name },
            titleColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor
        )

        IconButton(
            modifier = Modifier.size(32.dp),
            onClick = {
                if (sessionDetails.isStarred) {
                    unBookmarkSession(sessionDetails.id)
                } else {
                    bookmarkSession(sessionDetails.id)
                }
            }
        ) {
            Icon(
                modifier = Modifier
                    .testTag(TestTag.FAVOURITE_ICON),
                imageVector = if (sessionDetails.isStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = stringResource(R.string.star_session_icon_description),
                tint = if (sessionDetails.isStarred) ChaiRed else MaterialTheme.chaiColorsPalette.secondaryButtonColor
            )
        }
    }
}

@Composable
private fun SessionTitleAndDescription(
    sessionDetails: SessionDetailsPresentationModel
) {
    ChaiBodyLargeBold(
        modifier = Modifier.testTag(TestTag.SESSION_TITLE),
        bodyText = sessionDetails.title,
        textColor = MaterialTheme.chaiColorsPalette.textNormalColor
    )

    Spacer(modifier = Modifier.height(15.dp))

    ChaiBodyMedium(
        modifier = Modifier.testTag(TestTag.SESSION_DESCRIPTION),
        bodyText = sessionDetails.description,
        textColor = MaterialTheme.chaiColorsPalette.textWeakColor
    )
}

@Composable
private fun SessionLevel(sessionLevel: String) {
    ChaiBodySmall(
        modifier = Modifier
            .background(
                color = MaterialTheme.chaiColorsPalette.badgeBackgroundColor,
                shape = RoundedCornerShape(5.dp)
            )
            .padding(vertical = 3.dp, horizontal = 9.dp)
            .testTag(TestTag.LEVEL),
        bodyText = "#$sessionLevel".uppercase(),
        textColor = ChaiWhite
    )
}

@Composable
private fun SessionTimeAndRoom(
    sessionDetails: SessionDetailsPresentationModel
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        ChaiBodyXSmall(
            modifier = Modifier.testTag(TestTag.TIME_SLOT),
            bodyText = sessionDetails.timeSlot.uppercase(),
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        ChaiBodyXSmall(
            bodyText = "|",
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        ChaiBodyXSmall(
            modifier = Modifier.testTag(TestTag.ROOM),
            bodyText = sessionDetails.venue.uppercase(),
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor
        )
    }
}

@Composable
private fun CustomDivider() {
    Divider(
        thickness = 1.dp,
        color = MaterialTheme.chaiColorsPalette.surfaces
    )
}

@Composable
private fun TopBar(onNavigationIconClick: () -> Unit) {
    SmallTopAppBar(
        modifier = Modifier.testTag(TestTag.TOP_BAR),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.chaiColorsPalette.background,
            navigationIconContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
            scrolledContainerColor = MaterialTheme.chaiColorsPalette.background,
            titleContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
            actionIconContentColor = MaterialTheme.chaiColorsPalette.textBoldColor
        ),
        title = {
            ChaiBodyLarge(
                bodyText = stringResource(id = R.string.session_details_label),
                textColor = MaterialTheme.chaiColorsPalette.textBoldColor
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { onNavigationIconClick() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = stringResource(R.string.back_arrow_icon_description),
                    tint = MaterialTheme.chaiColorsPalette.textBoldColor
                )
            }
        }
    )
}

object TestTag {
    private const val PREFIX = "sessionDetailsScreen:"

    const val TOP_BAR = "$PREFIX topBar"
    const val FLOATING_ACTION_BUTTON = "$PREFIX floatingActionButton"
    const val SPEAKER_NAME = "$PREFIX speakerName"
    const val FAVOURITE_ICON = "$PREFIX favouriteIcon"
    const val SESSION_TITLE = "$PREFIX sessionTitle"
    const val SESSION_DESCRIPTION = "$PREFIX sessionDescription"
    const val IMAGE_BANNER = "$PREFIX imageBanner"
    const val TIME_SLOT = "$PREFIX timeSlot"
    const val ROOM = "$PREFIX room"
    const val LEVEL = "$PREFIX level"
    const val TWITTER_HANDLE_TEXT = "$PREFIX twitterHandleText"
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SessionDetailsScreenPreview() {
    ChaiDCKE22Theme() {
        SessionDetailsScreen(
            onNavigationIconClick = {},
            uiState = SessionDetailsUiState.Success(
                data = SessionDetailsPresentationModel(
                    id = "1",
                    title = "Welcome at DroidconKE",
                    description = "Welcome to DroidconKE 2022. We are excited to have you here. We hope you will have a great time.",
                    venue = "Main Hall",
                    startTime = "10:00",
                    endTime = "11:00",
                    amOrPm = "AM",
                    isStarred = false,
                    format = "Keynote",
                    level = "Beginner",
                    sessionImageUrl = "",
                    timeSlot = "10:00 - 11:00 AM",
                    speakers = listOf()
                )
            ),
            sessionId = "1",
            bookmarkSession = {},
            unBookmarkSession = {}
        )
    }
}