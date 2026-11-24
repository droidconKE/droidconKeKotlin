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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.adaptive.DroidconTooltip
import com.android254.presentation.common.adaptive.clickablePointer
import com.android254.presentation.common.adaptive.paneStartDivider
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.adaptive.rememberHorizontalHingeBounds
import com.android254.presentation.common.adaptive.rememberIsTabletopPosture
import com.android254.presentation.common.calendar.CalendarEvent
import com.android254.presentation.common.calendar.launchAddToCalendar
import com.android254.presentation.common.divider.CustomDivider
import com.android254.presentation.common.insets.DroidconWindowInsets
import com.android254.presentation.common.insets.StatusBarProtection
import com.android254.presentation.common.share.shareTextIntent
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.SessionDetailsViewModel
import com.android254.presentation.sessionDetails.view.components.SessionBannerImage
import com.android254.presentation.sessionDetails.view.components.SessionLevel
import com.android254.presentation.sessionDetails.view.components.SessionSpeakerNameAndFavouriteIcon
import com.android254.presentation.sessionDetails.view.components.SessionTimeAndRoom
import com.android254.presentation.sessionDetails.view.components.SessionTitleAndDescription
import com.android254.presentation.sessionDetails.view.components.SpeakerTwitterHandle
import com.android254.presentation.sessionDetails.view.components.TestTag
import com.android254.presentation.sessionDetails.view.components.TopBar
import com.android254.presentation.sessions.mappers.toCalendarEventOrNull
import com.android254.presentation.utils.FormFactorPreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SessionDetailsRoute(
    viewModel: SessionDetailsViewModel,
    onNavigationIconClick: () -> Unit,
    showTopBar: Boolean = true,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val noCalendarAppMessage = stringResource(R.string.calendar_no_app_available)
    SessionDetailsScreen(
        uiState = uiState,
        bookmarkSession = viewModel::bookmarkSession,
        unBookmarkSession = viewModel::unBookmarkSession,
        onNavigationIconClick = onNavigationIconClick,
        showTopBar = showTopBar,
        snackbarHostState = snackbarHostState,
        onShare = { session -> context.startActivity(shareIntent(session)) },
        onAddToCalendar = { event ->
            if (!launchAddToCalendar(context, event)) {
                coroutineScope.launch { snackbarHostState.showSnackbar(noCalendarAppMessage) }
            }
        },
    )
}

internal fun shareIntent(session: SessionDetailsPresentationModel) = shareTextIntent(subject = session.title, text = session.shareText())

internal fun SessionDetailsPresentationModel.shareText(): String =
    listOf(
        title,
        listOf(timeSlot, venue).filter(String::isNotBlank).joinToString(" · "),
        speakers.joinToString(", ") { it.name },
        DROIDCON_WEBSITE,
    ).filter(String::isNotBlank).joinToString("\n")

private const val DROIDCON_WEBSITE = "https://droidcon.co.ke"

@Composable
internal fun SessionDetailsScreen(
    uiState: SessionDetailsUiState,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
    onNavigationIconClick: () -> Unit,
    showTopBar: Boolean = true,
    isTabletop: Boolean = rememberIsTabletopPosture(),
    hingeBoundsInWindow: Rect? = rememberHorizontalHingeBounds(),
    onShare: (SessionDetailsPresentationModel) -> Unit = {},
    onAddToCalendar: ((CalendarEvent) -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val paneTitle = (uiState as? SessionDetailsUiState.Success)?.data?.title ?: stringResource(R.string.session_details_label)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val paneDividerColor = MaterialTheme.colorScheme.outlineVariant
    // The scrim draws over the content, so stack it here rather than rely on the caller.
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .then(if (!showTopBar) Modifier.paneStartDivider(paneDividerColor) else Modifier)
                .semantics { this.paneTitle = paneTitle },
    ) {
        Scaffold(
            modifier = if (showTopBar) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier,
            // As a pane the bar repeats the title and offers a back arrow out of a visible list.
            topBar = { if (showTopBar) TopBar(onNavigationIconClick = onNavigationIconClick, scrollBehavior = scrollBehavior) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (uiState is SessionDetailsUiState.Success) {
                    val shareLabel = stringResource(R.string.share_session)
                    DroidconTooltip(text = shareLabel) {
                        FloatingActionButton(
                            onClick = { onShare(uiState.data) },
                            modifier =
                                Modifier
                                    .size(44.dp)
                                    .clickablePointer()
                                    .testTag(TestTag.FLOATING_ACTION_BUTTON),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                        ) {
                            Icon(
                                modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                                imageVector = ChaiIcons.Reply,
                                contentDescription = shareLabel,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            },
            contentWindowInsets = DroidconWindowInsets.screenContent,
        ) { paddingValues ->
            when (uiState) {
                is SessionDetailsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is SessionDetailsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                    ) {
                        ChaiBodyMediumBold(
                            modifier = Modifier.align(Alignment.Center),
                            bodyText = uiState.message,
                            textColor = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                is SessionDetailsUiState.Success -> {
                    val calendarAction =
                        remember(uiState.data, onAddToCalendar) {
                            uiState.data.toCalendarEventOrNull()?.let { event ->
                                onAddToCalendar?.let { handler -> { handler(event) } }
                            }
                        }
                    if (isTabletop) {
                        TabletopBody(
                            paddingValues = paddingValues,
                            sessionDetails = uiState.data,
                            bookmarkSession = bookmarkSession,
                            unBookmarkSession = unBookmarkSession,
                            hingeBoundsInWindow = hingeBoundsInWindow,
                            onAddToCalendar = calendarAction,
                        )
                    } else {
                        Body(
                            paddingValues = paddingValues,
                            sessionDetails = uiState.data,
                            bookmarkSession = bookmarkSession,
                            unBookmarkSession = unBookmarkSession,
                            drawsUnderStatusBar = !showTopBar,
                            onAddToCalendar = calendarAction,
                        )
                    }
                }
            }
        }
        if (!showTopBar) {
            StatusBarProtection(modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

/** Half open: the banner above the fold, everything you touch below it. */
@Composable
private fun TabletopBody(
    paddingValues: PaddingValues,
    sessionDetails: SessionDetailsPresentationModel,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
    hingeBoundsInWindow: Rect?,
    modifier: Modifier = Modifier,
    onAddToCalendar: (() -> Unit)? = null,
) {
    var localHingeBounds by remember(hingeBoundsInWindow) { mutableStateOf<Pair<Int, Int>?>(null) }

    Layout(
        modifier =
            modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    localHingeBounds =
                        hingeBoundsInWindow?.let { hinge ->
                            val top = coordinates.windowToLocal(Offset(0f, hinge.top)).y.roundToInt()
                            val bottom = coordinates.windowToLocal(Offset(0f, hinge.bottom)).y.roundToInt()
                            top to bottom
                        }
                }.testTag(TestTag.TABLETOP_BODY),
        content = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .testTag("tabletop_banner"),
            ) {
                SessionBannerImage(
                    sessionDetails = sessionDetails,
                    fillAvailableBounds = true,
                )
            }
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp)
                        .testTag("tabletop_controls"),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                SessionSpeakerNameAndFavouriteIcon(
                    sessionDetails = sessionDetails,
                    bookmarkSession = bookmarkSession,
                    unBookmarkSession = unBookmarkSession,
                    onAddToCalendar = onAddToCalendar,
                )
                Spacer(modifier = Modifier.height(16.dp))
                SessionTitleAndDescription(sessionDetails)
                Spacer(modifier = Modifier.height(16.dp))
                SessionTimeAndRoom(sessionDetails)
                Spacer(modifier = Modifier.height(12.dp))
                SessionLevel(sessionDetails.level)
                Spacer(modifier = Modifier.height(24.dp))
            }
        },
    ) { measurables, constraints ->
        val totalHeight = constraints.maxHeight
        val hinge = localHingeBounds
        val hasValidHinge = hinge != null && hinge.first in 1 until totalHeight && hinge.second in hinge.first..totalHeight
        val topHeight = if (hasValidHinge) hinge.first else totalHeight / 2
        val bottomStart = if (hasValidHinge) hinge.second else topHeight
        val bottomHeight = (totalHeight - bottomStart).coerceAtLeast(0)

        val topPlaceable =
            measurables[0].measure(
                constraints.copy(minHeight = topHeight, maxHeight = topHeight),
            )
        val bottomPlaceable =
            measurables[1].measure(
                constraints.copy(minHeight = bottomHeight, maxHeight = bottomHeight),
            )
        layout(constraints.maxWidth, totalHeight) {
            topPlaceable.placeRelative(0, 0)
            bottomPlaceable.placeRelative(0, bottomStart)
        }
    }
}

@Composable
fun Body(
    paddingValues: PaddingValues,
    sessionDetails: SessionDetailsPresentationModel,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
    modifier: Modifier = Modifier,
    drawsUnderStatusBar: Boolean = false,
    onAddToCalendar: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .readablePaneWidth()
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Inside the scroll, so the title starts below the clock and then travels under it.
        if (drawsUnderStatusBar) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        }
        CustomDivider()
        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            SessionSpeakerNameAndFavouriteIcon(
                sessionDetails = sessionDetails,
                bookmarkSession = bookmarkSession,
                unBookmarkSession = unBookmarkSession,
                onAddToCalendar = onAddToCalendar,
            )

            Spacer(modifier = Modifier.height(25.dp))

            SessionTitleAndDescription(sessionDetails)

            Spacer(modifier = Modifier.height(15.dp))

            SessionBannerImage(sessionDetails)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier =
                Modifier
                    .padding(horizontal = 18.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SessionTimeAndRoom(sessionDetails)
            SessionLevel(sessionDetails.level)
            sessionDetails.speakers.forEach { speaker ->
                if (speaker.twitterHandle.isNotEmpty()) {
                    SpeakerTwitterHandle(speaker)
                }
            }
        }

        Spacer(modifier = Modifier.height(140.dp))
    }
}

@FormFactorPreviews
@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SessionDetailsScreenPreview() {
    ChaiTheme {
        SessionDetailsScreen(
            onNavigationIconClick = {},
            uiState =
                SessionDetailsUiState.Success(
                    data =
                        SessionDetailsPresentationModel(
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
                            speakers =
                                persistentListOf(
                                    SessionDetailsSpeakerPresentationModel(
                                        name = "Todd Jason",
                                        speakerImage = "",
                                        twitterHandle = "",
                                    ),
                                ),
                            startEpochMillis = PREVIEW_START_EPOCH_MILLIS,
                            endEpochMillis = PREVIEW_END_EPOCH_MILLIS,
                        ),
                ),
            bookmarkSession = {},
            unBookmarkSession = {},
            onAddToCalendar = {},
        )
    }
}

private const val PREVIEW_START_EPOCH_MILLIS = 1_793_862_000_000L
private const val PREVIEW_END_EPOCH_MILLIS = 1_793_865_600_000L