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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.models.speakersDummyData
import com.android254.presentation.speakers.SpeakerDetailsScreenUiState
import com.android254.presentation.speakers.SpeakerDetailsScreenViewModel
import com.droidconke.chai.atoms.ChaiBlue
import ke.droidcon.kotlin.presentation.R

@Composable
fun SpeakerDetailsRoute(
    id: Int,
    speakersDetailsScreenViewModel: SpeakerDetailsScreenViewModel = hiltViewModel(),
    navigateBack: () -> Unit = {}
) {
    val uiState = speakersDetailsScreenViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = true) {
        speakersDetailsScreenViewModel.getSpeakerById(id = id)
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
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = uiState.message
                )
            }
        }

        is SpeakerDetailsScreenUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = uiState.message
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
                            BorderStroke(2.dp, color = colorResource(id = R.color.cyan)),
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
                    Text(
                        text = stringResource(id = R.string.speaker_details_label),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                        color = colorResource(id = R.color.red_orange),
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = speaker.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        color = colorResource(id = R.color.blue),
                        modifier = Modifier
                            .testTag("speaker_name")
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = speaker.tagline.toString(),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .testTag("speaker_tagline")
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    )

                    Text(
                        text = stringResource(id = R.string.bio_label),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp,
                        color = colorResource(id = R.color.blue)
                    )

                    Text(
                        text = speaker.bio ?: "",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .testTag("speaker_bio")
                            .padding(top = 8.dp)
                    )
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            color = colorResource(
                                id = R.color.light_blue
                            )
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
                    Text(
                        text = stringResource(R.string.twitter_handle_label),
                        fontSize = 16.sp,
                        lineHeight = 19.sp
                    )

                    OutlinedButton(
                        onClick = {
                            if (speaker.twitterHandle != null) {
                                uriHandler.openUri(speaker.twitterHandle.toString())
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colorResource(id = R.color.blue)),
                        modifier = Modifier
                            .testTag("twitter_button")
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_twitter),
                            contentDescription = stringResource(id = R.string.share),
                            tint = ChaiBlue
                        )
                        Text(
                            text = if (speaker.twitterHandle != null) {
                                speaker.twitterHandle.toString()
                                    .replace("https://twitter.com/", "")
                            } else {
                                ""
                            },
                            fontSize = 16.sp,
                            lineHeight = 19.sp,
                            color = colorResource(id = R.color.blue),
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
    DroidconKE2023Theme {
        SpeakerDetailsScreen(
            uiState = SpeakerDetailsScreenUiState.Success(
                speaker = speakersDummyData.first()
            )
        )
    }
}