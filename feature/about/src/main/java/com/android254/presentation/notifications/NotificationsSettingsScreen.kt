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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.adaptive.readablePaneWidth
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
private val RowSpacing = 12.dp

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationsSettingsContent(
    uiState: NotificationsUiState,
    onChannelEnabledChange: (NotificationChannelSpec, Boolean) -> Unit,
    onReminderLeadTimeChange: (Int) -> Unit,
    onOpenSystemSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings_notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = ChaiR.drawable.ic_back_arrow),
                            contentDescription = stringResource(R.string.back_arrow_icon_description),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = DroidconWindowInsets.screenContent,
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .readablePaneWidth(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                AnimatedVisibility(visible = uiState.notificationsBlocked) {
                    NotificationsBlockedBanner(
                        onOpenSystemSettings = onOpenSystemSettings,
                        modifier = Modifier.padding(horizontal = ContentPadding, vertical = RowSpacing),
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.settings_notification_types),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .padding(horizontal = ContentPadding, vertical = RowSpacing)
                            .semantics { heading() },
                )
            }

            items(uiState.channels, key = { it.channel.id }) { state ->
                val remindersChannel = state.channel == NotificationChannelSpec.SessionReminders
                Column(modifier = Modifier.fillMaxWidth()) {
                    ChannelRow(
                        state = state,
                        interactive = !uiState.notificationsBlocked,
                        onCheckedChange = { onChannelEnabledChange(state.channel, it) },
                    )
                    if (remindersChannel) {
                        AnimatedVisibility(visible = state.enabled && !state.blockedInSystem) {
                            LeadTimeChoice(
                                selectedMinutes = uiState.reminderLeadTimeMinutes,
                                enabled = !uiState.notificationsBlocked,
                                onSelected = onReminderLeadTimeChange,
                            )
                        }
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = RowSpacing))
                SystemSettingsRow(onClick = onOpenSystemSettings)
            }
        }
    }
}

@Composable
private fun ChannelRow(
    state: ChannelUiState,
    interactive: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val checked = state.enabled && !state.blockedInSystem
    val enabled = interactive && !state.blockedInSystem
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(horizontal = ContentPadding, vertical = RowSpacing),
        horizontalArrangement = Arrangement.spacedBy(RowSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(state.channel.nameResId),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text =
                    if (state.blockedInSystem) {
                        stringResource(R.string.settings_channel_off_in_system)
                    } else {
                        stringResource(state.channel.descriptionResId)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeadTimeChoice(
    selectedMinutes: Int,
    enabled: Boolean,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = ContentPadding, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_reminder_lead_time),
            style = MaterialTheme.typography.labelLarge,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ReminderLeadTime.entries.forEachIndexed { index, leadTime ->
                SegmentedButton(
                    selected = selectedMinutes == leadTime.minutes,
                    onClick = { onSelected(leadTime.minutes) },
                    enabled = enabled,
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ReminderLeadTime.entries.size),
                ) {
                    Text(stringResource(R.string.settings_lead_time_option, leadTime.minutes))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun SystemSettingsRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = ContentPadding, vertical = RowSpacing),
        horizontalArrangement = Arrangement.spacedBy(RowSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.settings_system_notifications),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = ChaiIcons.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

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
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(ContentPadding),
            verticalArrangement = Arrangement.spacedBy(RowSpacing),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.settings_notifications_blocked),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.settings_notifications_blocked_detail),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(onClick = onOpenSystemSettings, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.settings_open_system_settings))
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