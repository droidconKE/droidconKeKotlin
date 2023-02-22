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
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.android254.droidconKE2023.presentation.R
import com.android254.presentation.common.theme.DroidconKE2022Theme
import com.android254.presentation.common.theme.Montserrat
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsViewModel

@Composable
fun SessionDetailsScreen(
    darkTheme: Boolean = isSystemInDarkTheme(),
    viewModel: SessionDetailsViewModel = hiltViewModel(),
    sessionId: String,
    onNavigationIconClick: () -> Unit,
) {

    val sessionDetails by viewModel.sessionDetails.observeAsState()

    LaunchedEffect(key1 = sessionId) {
        viewModel.getSessionDetailsById(sessionId)
    }

    Scaffold(
        topBar = { TopBar(onNavigationIconClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                modifier = Modifier
                    .size(44.dp)
                    .testTag(TestTag.FLOATING_ACTION_BUTTON),
                containerColor = colorResource(id = R.color.red_orange),
                shape = CircleShape
            ) {
                Icon(
                    modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                    imageVector = Icons.Filled.Reply,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->

        sessionDetails?.let {
            Body(paddingValues, darkTheme, it)
        }
    }
}

@Composable
private fun Body(
    paddingValues: PaddingValues,
    darkTheme: Boolean,
    sessionDetails: SessionDetailsPresentationModel
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

            SessionSpeakerNameAndFavouriteIcon(darkTheme, sessionDetails)

            Spacer(modifier = Modifier.height(25.dp))

            SessionTitleAndDescription(darkTheme, sessionDetails)

            Spacer(modifier = Modifier.height(15.dp))

            SessionBannerImage(sessionDetails)
        }

        Spacer(modifier = Modifier.height(25.dp))

        CustomDivider()

        Spacer(modifier = Modifier.height(19.dp))

        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp)) {

            SessionTimeAndRoom(darkTheme, sessionDetails)

            Spacer(modifier = Modifier.height(15.dp))

            SessionLevel(darkTheme, sessionDetails.level)

            Spacer(modifier = Modifier.height(18.dp))
        }

        CustomDivider()

        Spacer(modifier = Modifier.height(18.dp))

        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp)) {
            SpeakerTwitterHandle(darkTheme, sessionDetails)
        }

        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
private fun SpeakerTwitterHandle(
    darkTheme: Boolean,
    sessionDetails: SessionDetailsPresentationModel
) {

    if (sessionDetails.twitterHandle != null) {
        val context = LocalContext.current
        val intent = remember { Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/${sessionDetails.twitterHandle}")) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(id = R.string.twitter_handle_label),
                color = colorResource(id = if (darkTheme) R.color.smoke_white else R.color.dark),
                style = TextStyle(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 19.sp
                )
            )
            Button(
                onClick = { context.startActivity(intent) },
                border = BorderStroke(
                    1.dp,
                    colorResource(id = if (darkTheme) R.color.cyan else R.color.blue)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 40.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(colorResource(id = if (darkTheme) R.color.black else R.color.white))
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.ic_twitter_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .height(20.dp)
                        .width(20.dp),
                    tint = colorResource(id = if (darkTheme) R.color.aqua else R.color.blue)
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    modifier = Modifier.testTag(TestTag.TWITTER_HANDLE_TEXT),
                    text = sessionDetails.twitterHandle,
                    color = colorResource(id = if (darkTheme) R.color.aqua else R.color.blue),
                    style = TextStyle(
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 19.sp
                    ),
                )
            }
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
            .border(1.dp, colorResource(id = R.color.aqua), RoundedCornerShape(10.dp))
            .testTag(TestTag.IMAGE_BANNER)
    )
}

@Composable
private fun SessionSpeakerNameAndFavouriteIcon(
    darkTheme: Boolean,
    sessionDetails: SessionDetailsPresentationModel
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
            tint = colorResource(R.color.red_orange)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = stringResource(id = R.string.speaker_label),
            color = colorResource(id = R.color.red_orange),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Normal
            )
        )
    }

    Row(
        modifier = Modifier
            .padding(end = 15.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            modifier = Modifier.testTag(TestTag.SPEAKER_NAME),
            text = sessionDetails.speakerName,
            color = colorResource(id = if (darkTheme) R.color.white else R.color.blue),
            style = TextStyle(
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 18.sp
            )
        )

        Icon(
            imageVector = if (sessionDetails.isStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
            contentDescription = null,
            modifier = Modifier
                .size(21.dp)
                .testTag(TestTag.FAVOURITE_ICON),
            tint = colorResource(id = if (darkTheme) R.color.cyan else R.color.blue)
        )
    }
}

@Composable
private fun SessionTitleAndDescription(
    darkTheme: Boolean,
    sessionDetails: SessionDetailsPresentationModel
) {
    Text(
        modifier = Modifier.testTag(TestTag.SESSION_TITLE),
        text = sessionDetails.title,
        color = colorResource(id = if (darkTheme) R.color.white else R.color.eerie_black),
        style = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 22.sp
        )
    )

    Spacer(modifier = Modifier.height(15.dp))

    Text(
        modifier = Modifier.testTag(TestTag.SESSION_DESCRIPTION),
        text = sessionDetails.description,
        color = colorResource(id = if (darkTheme) R.color.smoke_white else R.color.grey),
        style = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Light,
            fontSize = 16.sp,
            lineHeight = 19.sp
        )
    )
}

@Composable
private fun SessionLevel(darkTheme: Boolean = isSystemInDarkTheme(), sessionLevel: String) {
    Text(
        text = "#$sessionLevel".uppercase(),
        style = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = Color.White
        ),
        modifier = Modifier
            .background(
                colorResource(id = if (darkTheme) R.color.black else R.color.eerie_black),
                RoundedCornerShape(5.dp)
            )
            .padding(vertical = 3.dp, horizontal = 9.dp)
            .testTag(TestTag.LEVEL)
    )
}

@Composable
private fun SessionTimeAndRoom(
    darkTheme: Boolean = isSystemInDarkTheme(),
    sessionDetails: SessionDetailsPresentationModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.testTag(TestTag.TIME_SLOT),
            text = sessionDetails.timeSlot.uppercase(),
            color = colorResource(id = if (darkTheme) R.color.light_grey else R.color.grey),
            style = TextStyle(
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = "|",
            color = colorResource(id = if (darkTheme) R.color.light_grey else R.color.grey),
            style = TextStyle(
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            modifier = Modifier.testTag(TestTag.ROOM),
            text = sessionDetails.venue.uppercase(),
            color = colorResource(id = if (darkTheme) R.color.light_grey else R.color.grey),
            style = TextStyle(
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        )
    }
}

@Composable
private fun CustomDivider(darkTheme: Boolean = isSystemInDarkTheme()) {
    Divider(
        thickness = 1.dp,
        color = colorResource(id = if (darkTheme) R.color.black else R.color.smoke_white)
    )
}

@Composable
private fun TopBar(onNavigationIconClick: () -> Unit) {
    SmallTopAppBar(
        modifier = Modifier.testTag(TestTag.TOP_BAR),
        title = {
            Text(
                text = stringResource(id = R.string.session_details_label),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    lineHeight = 28.sp
                ),
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { onNavigationIconClick() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = stringResource(R.string.back_arrow_icon_description),
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun SessionDetailsScreenPreview() {
    DroidconKE2022Theme(darkTheme = false) {
        SessionDetailsScreen(
            onNavigationIconClick = {},
            sessionId = "1",
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun SessionDetailsScreenDarkThemePreview() {
    DroidconKE2022Theme(darkTheme = true) {
        SessionDetailsScreen(
            onNavigationIconClick = {},
            sessionId = "1",
        )
    }
}