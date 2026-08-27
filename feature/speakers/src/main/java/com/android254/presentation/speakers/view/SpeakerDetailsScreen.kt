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
package com.android254.presentation.speakers.view

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.components.SessionsCard
import com.android254.presentation.common.navigation.speakerSharedImage
import com.android254.presentation.common.navigation.speakerSharedName
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.speakers.SpeakerDetailsScreenUiState
import com.android254.presentation.speakers.SpeakerDetailsScreenViewModel
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.CButton
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiTitle
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.persistentListOf
import ke.droidcon.kotlin.chai.R as ChaiR

private const val SPEAKER_IMAGE_ASPECT_RATIO = 1.35f
private val ScreenPadding = 24.dp

@Composable
fun SpeakerDetailsRoute(
    name: String,
    speakersDetailsScreenViewModel: SpeakerDetailsScreenViewModel = hiltViewModel(),
    navigateBack: () -> Unit = {},
    navigateToSessionDetails: (String) -> Unit = {},
) {
    val uiState by speakersDetailsScreenViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = name) {
        speakersDetailsScreenViewModel.getSpeakerByName(name = name)
    }

    SpeakerDetailsScreen(
        uiState = uiState,
        navigateBack = navigateBack,
        navigateToSessionDetails = navigateToSessionDetails,
        onBookmark = speakersDetailsScreenViewModel::onBookmark,
    )
}

@Composable
internal fun SpeakerDetailsScreen(
    uiState: SpeakerDetailsScreenUiState,
    navigateBack: () -> Unit = {},
    navigateToSessionDetails: (String) -> Unit = {},
    onBookmark: (String) -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    ChaiBodyLargeBold(
                        bodyText = stringResource(id = R.string.speaker_details_label),
                        textColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(id = ChaiR.drawable.ic_back_arrow),
                            contentDescription = stringResource(R.string.back_arrow_icon_description),
                            tint = MaterialTheme.chaiColorsPalette.textBoldColor,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                        navigationIconContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                    ),
            )
        },
        containerColor = MaterialTheme.chaiColorsPalette.background,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is SpeakerDetailsScreenUiState.SpeakerNotFound ->
                    CenteredMessage(uiState.message)

                is SpeakerDetailsScreenUiState.Error ->
                    CenteredMessage(uiState.message)

                is SpeakerDetailsScreenUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is SpeakerDetailsScreenUiState.Success ->
                    SpeakerDetailsContent(
                        uiState = uiState,
                        navigateToSessionDetails = navigateToSessionDetails,
                        onBookmark = onBookmark,
                    )
            }
        }
    }
}

@Composable
private fun SpeakerDetailsContent(
    uiState: SpeakerDetailsScreenUiState.Success,
    navigateToSessionDetails: (String) -> Unit,
    onBookmark: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val speaker = uiState.speaker

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        HorizontalDivider(color = MaterialTheme.chaiColorsPalette.cardsBorderColor)

        Column(modifier = Modifier.padding(horizontal = ScreenPadding)) {
            Spacer(modifier = Modifier.height(24.dp))

            ChaiTitle(
                modifier =
                    Modifier
                        .testTag("speaker_name")
                        .speakerSharedName(speaker.name),
                titleText = speaker.name,
                titleColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor,
            )

            speaker.tagline?.takeIf(String::isNotBlank)?.let { tagline ->
                Spacer(modifier = Modifier.height(8.dp))
                ChaiBodyMedium(
                    modifier = Modifier.testTag("speaker_tagline"),
                    bodyText = tagline,
                    textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(speaker.imageUrl)
                        .build(),
                placeholder = painterResource(R.drawable.smiling),
                contentDescription = stringResource(R.string.head_shot),
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .testTag("speaker_image")
                        .fillMaxWidth()
                        .aspectRatio(SPEAKER_IMAGE_ASPECT_RATIO)
                        .speakerSharedImage(speaker.name, RoundedCornerShape(16.dp)),
            )

            speaker.bio?.takeIf(String::isNotBlank)?.let { bio ->
                Spacer(modifier = Modifier.height(24.dp))
                ChaiBodyMedium(
                    modifier = Modifier.testTag("speaker_bio"),
                    bodyText = bio,
                    textColor = MaterialTheme.chaiColorsPalette.textNormalColor,
                )
            }

            if (uiState.sessions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                uiState.sessions.forEach { session ->
                    SessionsCard(
                        modifier = Modifier.testTag("speaker_session_${session.id}"),
                        session = session,
                        navigateToSessionDetails = navigateToSessionDetails,
                        onBookmark = onBookmark,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TwitterHandleRow(twitterHandle = speaker.twitterHandle)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TwitterHandleRow(
    twitterHandle: String?,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ChaiBodyMedium(
            bodyText = stringResource(R.string.twitter_handle_label),
            textColor = MaterialTheme.chaiColorsPalette.textNormalColor,
        )

        CButton(
            modifier =
                Modifier
                    .testTag("twitter_button")
                    .border(
                        border =
                            BorderStroke(
                                1.dp,
                                MaterialTheme.chaiColorsPalette.secondaryButtonColor,
                            ),
                        shape = RoundedCornerShape(10.dp),
                    ).clip(RoundedCornerShape(10.dp)),
            onClick = {
                if (!twitterHandle.isNullOrBlank()) {
                    uriHandler.openUri(twitterHandle)
                }
            },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.chaiColorsPalette.outlinedButtonBackgroundColor,
                    contentColor = MaterialTheme.chaiColorsPalette.secondaryButtonColor,
                    disabledContainerColor = MaterialTheme.chaiColorsPalette.outlinedButtonBackgroundColor,
                    disabledContentColor = MaterialTheme.chaiColorsPalette.secondaryButtonColor,
                ),
            shape = RoundedCornerShape(10.dp),
            isEnabled = true,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_twitter),
                contentDescription = stringResource(id = R.string.share),
                tint = MaterialTheme.chaiColorsPalette.secondaryButtonColor,
            )
            ChaiBodyMedium(
                modifier = Modifier.padding(start = 6.dp),
                bodyText = twitterHandle.orEmpty().substringAfterLast('/'),
                textColor = MaterialTheme.chaiColorsPalette.secondaryButtonColor,
            )
        }
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        ChaiBodyMediumBold(
            modifier = Modifier.align(Alignment.Center),
            bodyText = message,
            textColor = MaterialTheme.chaiColorsPalette.textNormalColor,
        )
    }
}

@Preview(
    name = "Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SpeakerDetailsScreenPreview() {
    ChaiTheme {
        SpeakerDetailsScreen(
            uiState =
                SpeakerDetailsScreenUiState.Success(
                    speaker =
                        SpeakerUI(
                            name = "Omolara Adejuwon",
                            tagline = "Android Engineer",
                            bio = "Hi there!\nThis is Omolara,\nWorking as Android Engineer.",
                            twitterHandle = "https://twitter.com/omolara_ade",
                        ),
                    sessions = persistentListOf(),
                ),
        )
    }
}