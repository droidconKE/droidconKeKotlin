/*
 * Copyright 2026 DroidconKE
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
package com.android254.presentation.notifications

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.components.ConnectedToggleGroup
import com.android254.presentation.common.insets.DroidconWindowInsets
import com.android254.presentation.utils.FormFactorPreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.notifications.preferences.ReminderLeadTime
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import ke.droidcon.kotlin.chai.R as ChaiR

private val ContentPadding = 20.dp
private val SectionSpacing = 12.dp
private val GroupSpacing = 24.dp
private val ControlSpacing = 8.dp
private val TightSpacing = 4.dp
private val BannerPadding = 16.dp
private val BannerBadgeSize = 40.dp
private val BannerIconSize = 22.dp
private val LeadTimeOptionPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)

private val LeadTimeOptions = ReminderLeadTime.entries.toImmutableList()

@Composable
fun NotificationsSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // The user can allow notifications in system settings and come straight back here.
    LifecycleResumeEffect(Unit) {
        viewModel.refreshSystemState()
        onPauseOrDispose {}
    }

    NotificationsSettingsContent(
        uiState = uiState,
        onChannelEnabledChange = viewModel::setChannelEnabled,
        onReminderLeadTimeChange = viewModel::setReminderLeadTime,
        onOpenSystemSettings = { openSystemNotificationSettings(context) },
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
internal fun NotificationsSettingsContent(
    uiState: NotificationsUiState,
    onChannelEnabledChange: (NotificationChannelSpec, Boolean) -> Unit,
    onReminderLeadTimeChange: (Int) -> Unit,
    onOpenSystemSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { NotificationsTopBar(onNavigateBack = onNavigateBack) },
        contentWindowInsets = DroidconWindowInsets.screenContent,
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .readablePaneWidth(),
            contentPadding = PaddingValues(start = ContentPadding, end = ContentPadding, bottom = GroupSpacing),
        ) {
            item {
                AnimatedVisibility(
                    visible = uiState.notificationsBlocked,
                    enter =
                        expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                            fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                    exit =
                        shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) +
                            fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
                ) {
                    NotificationsBlockedBanner(
                        onOpenSystemSettings = onOpenSystemSettings,
                        modifier = Modifier.padding(top = SectionSpacing),
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.settings_notification_types),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .padding(
                                start = ListItemDefaults.ContentPadding.calculateStartPadding(LocalLayoutDirection.current),
                                top = GroupSpacing,
                                bottom = ControlSpacing,
                            ).semantics { heading() },
                )
            }

            item {
                ChannelGroup(
                    uiState = uiState,
                    onChannelEnabledChange = onChannelEnabledChange,
                    onReminderLeadTimeChange = onReminderLeadTimeChange,
                )
            }

            item {
                SystemSettingsItem(
                    onClick = onOpenSystemSettings,
                    modifier = Modifier.padding(top = GroupSpacing),
                )
            }
        }
    }
}

@Composable
private fun NotificationsTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        title = {
            Text(
                text = stringResource(R.string.settings_notifications_title),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(ChaiR.drawable.ic_back_arrow),
                    contentDescription = stringResource(R.string.back_arrow_icon_description),
                )
            }
        },
    )
}

@Composable
private fun ChannelGroup(
    uiState: NotificationsUiState,
    onChannelEnabledChange: (NotificationChannelSpec, Boolean) -> Unit,
    onReminderLeadTimeChange: (Int) -> Unit,
) {
    val showLeadTime = uiState.sessionRemindersEnabled
    val leadTimeIndex = uiState.channels.indexOfFirst { it.channel == NotificationChannelSpec.SessionReminders } + 1
    val count = uiState.channels.size + if (showLeadTime) 1 else 0

    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        uiState.channels.forEachIndexed { position, state ->
            ChannelItem(
                state = state,
                notificationsBlocked = uiState.notificationsBlocked,
                shapes =
                    ListItemDefaults.segmentedShapes(
                        index = if (showLeadTime && position >= leadTimeIndex) position + 1 else position,
                        count = count,
                    ),
                onCheckedChange = { onChannelEnabledChange(state.channel, it) },
            )
            if (position + 1 == leadTimeIndex) {
                AnimatedVisibility(
                    visible = showLeadTime,
                    enter =
                        expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                            fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                    exit =
                        shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) +
                            fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
                ) {
                    LeadTimeItem(
                        selectedMinutes = uiState.reminderLeadTimeMinutes,
                        shapes = ListItemDefaults.segmentedShapes(index = leadTimeIndex, count = count),
                        onSelected = onReminderLeadTimeChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelItem(
    state: ChannelUiState,
    notificationsBlocked: Boolean,
    shapes: ListItemShapes,
    onCheckedChange: (Boolean) -> Unit,
) {
    val enabled = !notificationsBlocked && !state.blockedInSystem
    val checked = enabled && state.enabled
    SegmentedListItem(
        onClick = { onCheckedChange(!checked) },
        shapes = shapes,
        modifier =
            Modifier.semantics {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
            },
        enabled = enabled,
        colors = segmentColors(),
        supportingContent = {
            Text(
                text =
                    if (state.blockedInSystem) {
                        stringResource(R.string.settings_channel_off_in_system)
                    } else {
                        stringResource(state.channel.descriptionResId)
                    },
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                thumbContent =
                    if (checked) {
                        {
                            Icon(
                                imageVector = ChaiIcons.CheckRounded,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    },
            )
        },
    ) {
        Text(text = stringResource(state.channel.nameResId))
    }
}

@Composable
private fun LeadTimeItem(
    selectedMinutes: Int,
    shapes: ListItemShapes,
    onSelected: (Int) -> Unit,
) {
    SegmentedListItem(
        shapes = shapes,
        colors = segmentColors(),
        supportingContent = {
            ConnectedToggleGroup(
                items = LeadTimeOptions,
                selected = LeadTimeOptions.firstOrNull { it.minutes == selectedMinutes },
                onSelect = { onSelected(it.minutes) },
                modifier = Modifier.padding(top = ControlSpacing),
                fillWidth = true,
                contentPadding = LeadTimeOptionPadding,
            ) { leadTime ->
                Text(
                    text = stringResource(R.string.settings_lead_time_option, leadTime.minutes),
                    textAlign = TextAlign.Center,
                )
            }
        },
    ) {
        Text(text = stringResource(R.string.settings_reminder_lead_time))
    }
}

@Composable
private fun SystemSettingsItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
        modifier = modifier.semantics { role = Role.Button },
        colors = segmentColors(),
        trailingContent = {
            Icon(
                imageVector = ChaiIcons.OpenInNew,
                contentDescription = null,
            )
        },
    ) {
        Text(text = stringResource(R.string.settings_system_notifications))
    }
}

@Composable
private fun segmentColors() =
    ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )

/** Opens the app's notification settings, or its details screen where a ROM has removed that. */
internal fun openSystemNotificationSettings(context: Context) {
    val notificationSettings =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    val appDetails =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    val opened =
        listOf(notificationSettings, appDetails).any { intent ->
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                true
            } catch (e: ActivityNotFoundException) {
                Timber.w(e, "No activity for ${intent.action}")
                false
            }
        }
    if (!opened) Timber.w("Could not open any system notification settings")
}

@Composable
private fun NotificationsBlockedBanner(
    onOpenSystemSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = ListItemDefaults.segmentedShapes(index = 0, count = 1).shape,
    ) {
        Row(
            modifier = Modifier.padding(BannerPadding),
            horizontalArrangement = Arrangement.spacedBy(SectionSpacing),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(BannerBadgeSize)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ChaiIcons.NotificationsOffOutlined,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(BannerIconSize),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(TightSpacing)) {
                Text(
                    text = stringResource(R.string.settings_notifications_blocked),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                Text(
                    text = stringResource(R.string.settings_notifications_blocked_detail),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = onOpenSystemSettings,
                    modifier =
                        Modifier
                            .align(Alignment.End)
                            .padding(top = ControlSpacing),
                ) {
                    Text(stringResource(R.string.settings_open_system_settings))
                }
            }
        }
    }
}

@FormFactorPreviews
@Composable
private fun NotificationsSettingsPreview() {
    ChaiTheme {
        NotificationsSettingsContent(
            uiState = sampleNotificationsUiState(),
            onChannelEnabledChange = { _, _ -> },
            onReminderLeadTimeChange = {},
            onOpenSystemSettings = {},
            onNavigateBack = {},
        )
    }
}

@FormFactorPreviews
@Composable
private fun NotificationsSettingsBlockedPreview() {
    ChaiTheme {
        NotificationsSettingsContent(
            uiState = sampleNotificationsUiState().copy(notificationsBlocked = true),
            onChannelEnabledChange = { _, _ -> },
            onReminderLeadTimeChange = {},
            onOpenSystemSettings = {},
            onNavigateBack = {},
        )
    }
}

internal fun sampleNotificationsUiState() =
    NotificationsUiState(
        channels =
            NotificationChannelSpec.configurable
                .map { ChannelUiState(channel = it, enabled = true, blockedInSystem = false) }
                .toImmutableList(),
    )