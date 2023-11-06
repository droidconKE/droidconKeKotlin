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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.models.speakersDummyData
import com.android254.presentation.speakers.SpeakerDetailsScreenUiState
import com.android254.presentation.speakers.SpeakerDetailsScreenViewModel
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.atoms.ChaiTeal
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.CButton
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiBodySmall
import com.droidconke.chai.components.ChaiSubTitle
import com.droidconke.chai.components.ChaiTextLabelLarge
import com.droidconke.chai.components.ChaiTitle
import ke.droidcon.kotlin.presentation.R

@Composable
fun SpeakerDetailsRoute(
    name: String,
    speakersDetailsScreenViewModel: SpeakerDetailsScreenViewModel = hiltViewModel(),
    navigateBack: () -> Unit = {}
) {
    val uiState = speakersDetailsScreenViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = true) {
        speakersDetailsScreenViewModel.getSpeakerByName(name = name)
    }

    SpeakerDetailsScreen(
        uiState = uiState.value,
        navigateBack = navigateBack
    )
}

@Composable
private fun SpeakerDetailsScreen(
    uiState: SpeakerDetailsScreenUiState,
    navigateBack: () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current

    when (uiState) {
        is SpeakerDetailsScreenUiState.SpeakerNotFound -> {
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

        is SpeakerDetailsScreenUiState.Error -> {
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

        is SpeakerDetailsScreenUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        is SpeakerDetailsScreenUiState.Success -> {
            val speaker = uiState.speaker
            ConstraintLayout(
                modifier = Modifier
                    .background(color = MaterialTheme.chaiColorsPalette.background)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                val (topBar, speakerDetails, speakerImage, divider, twitterContainer) = createRefs()

                TopAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                        .constrainAs(topBar) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                        },
                    onBackPressed = navigateBack
                )

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(speaker.imageUrl)
                        .build(),
                    placeholder = painterResource(R.drawable.smiling),
                    contentDescription = stringResource(R.string.head_shot),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .testTag("speaker_image")
                        .clip(
                            shape = CircleShape
                        )
                        .border(
                            BorderStroke(2.dp, color = ChaiTeal),
                            shape = CircleShape
                        )
                        .size(105.dp)
                        .constrainAs(speakerImage) {
                            top.linkTo(topBar.bottom)
                            bottom.linkTo(topBar.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                        .constrainAs(speakerDetails) {
                            top.linkTo(speakerImage.bottom, 10.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                ) {
                    ChaiTextLabelLarge(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .align(Alignment.CenterHorizontally),
                        bodyText = stringResource(id = R.string.speaker_details_label),
                        textColor = ChaiRed
                    )

                    ChaiTitle(
                        modifier = Modifier
                            .testTag("speaker_name")
                            .align(Alignment.CenterHorizontally),
                        titleText = speaker.name,
                        titleColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor
                    )

                    ChaiBodySmall(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .testTag("speaker_tagline")
                            .align(Alignment.CenterHorizontally),
                        bodyText = speaker.tagline.toString(),
                        textAlign = TextAlign.Center,
                        textColor = MaterialTheme.chaiColorsPalette.textWeakColor
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    )

                    ChaiSubTitle(
                        titleText = stringResource(id = R.string.bio_label),
                        titleColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor
                    )

                    ChaiBodySmall(
                        modifier = Modifier
                            .testTag("speaker_bio")
                            .padding(top = 8.dp),
                        bodyText = speaker.bio ?: "",
                        textColor = MaterialTheme.chaiColorsPalette.textNormalColor
                    )
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            color = MaterialTheme.chaiColorsPalette.surfaces
                        )
                        .constrainAs(divider) {
                            top.linkTo(speakerDetails.bottom, 20.dp)
                        }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 20.dp)
                        .constrainAs(twitterContainer) {
                            top.linkTo(divider.bottom)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ChaiBodyMedium(
                        bodyText = stringResource(R.string.twitter_handle_label),
                        textColor = MaterialTheme.chaiColorsPalette.textNormalColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    CButton(
                        modifier = Modifier
                            .testTag("twitter_button")
                            .align(Alignment.CenterVertically)
                            .border(
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.chaiColorsPalette.secondaryButtonColor
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp)),
                        onClick = {
                            if (speaker.twitterHandle != null) {
                                uriHandler.openUri(speaker.twitterHandle.toString())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.chaiColorsPalette.outlinedButtonBackgroundColor,
                            contentColor = MaterialTheme.chaiColorsPalette.secondaryButtonColor,
                            disabledContainerColor = MaterialTheme.chaiColorsPalette.outlinedButtonBackgroundColor,
                            disabledContentColor = MaterialTheme.chaiColorsPalette.secondaryButtonColor
                        ),
                        shape = RoundedCornerShape(10.dp),
                        isEnabled = true
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_twitter),
                            contentDescription = stringResource(id = R.string.share),
                            tint = MaterialTheme.chaiColorsPalette.secondaryButtonColor
                        )
                        ChaiBodyMedium(
                            bodyText = if (speaker.twitterHandle != null) {
                                speaker.twitterHandle.toString()
                                    .replace("https://twitter.com/", "")
                            } else {
                                ""
                            },
                            textColor = MaterialTheme.chaiColorsPalette.secondaryButtonColor,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SpeakerDetailsScreenPreview() {
    ChaiDCKE22Theme {
        SpeakerDetailsScreen(
            uiState = SpeakerDetailsScreenUiState.Success(
                speaker = speakersDummyData.first()
            )
        )
    }
}