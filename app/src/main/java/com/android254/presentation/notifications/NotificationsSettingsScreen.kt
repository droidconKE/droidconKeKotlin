/*
 * Copyright 2025 DroidconKE
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.insets.DroidconWindowInsets
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.notifications.preferences.ReminderLeadTime
import ke.droidcon.kotlin.core.ui.R
import timber.log.Timber
import ke.droidcon.kotlin.chai.R as ChaiR
import ke.droidcon.kotlin.core.notifications.R as NotificationsR

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
        onMasterEnabledChange = viewModel::setMasterEnabled,
        onChannelEnabledChange = viewModel::setChannelEnabled,
        onReminderLeadTimeChange = viewModel::setReminderLeadTime,
        onOpenSystemSettings = { openSystemNotificationSettings(context) },
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsContent(
    uiState: NotificationsUiState,
    onMasterEnabledChange: (Boolean) -> Unit,
    onChannelEnabledChange: (NotificationChannelSpec, Boolean) -> Unit,
    onReminderLeadTimeChange: (Int) -> Unit,
    onOpenSystemSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = ChaiR.drawable.ic_back_arrow),
                            contentDescription = stringResource(R.string.back_arrow_icon_description),
                        )
                    }
                },
            )
        },
        contentWindowInsets = DroidconWindowInsets.screenContent,
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (uiState.notificationsBlocked) {
                item {
                    NotificationsBlockedBanner(
                        onOpenSystemSettings = onOpenSystemSettings,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            item {
                SwitchListItem(
                    checked = uiState.masterEnabled,
                    onCheckedChange = onMasterEnabledChange,
                    headline = { Text(stringResource(R.string.settings_master_toggle)) },
                    supportingText = { Text(stringResource(R.string.settings_master_toggle_description)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (uiState.masterEnabled) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                item {
                    Text(
                        stringResource(R.string.settings_notification_types),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                items(NotificationChannelSpec.entries.size) { index ->
                    val channel = NotificationChannelSpec.entries[index]
                    val isEnabled = uiState.channelStates[channel] ?: false
                    SwitchListItem(
                        checked = isEnabled,
                        onCheckedChange = { onChannelEnabledChange(channel, it) },
                        headline = { Text(getChannelDisplayName(channel)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                item {
                    Text(
                        stringResource(R.string.settings_reminder_lead_time),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ReminderLeadTime.entries.forEach { leadTime ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = uiState.reminderLeadTimeMinutes == leadTime.minutes,
                                    onClick = { onReminderLeadTimeChange(leadTime.minutes) },
                                )
                                Spacer(modifier = Modifier.padding(start = 8.dp))
                                Text(
                                    getLeadTimeLabel(leadTime),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 8.dp)) {
            Text(
                stringResource(R.string.settings_notifications_blocked),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 8.dp),
            )
            TextButton(
                onClick = onOpenSystemSettings,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.settings_open_system_settings))
            }
        }
    }
}

@Composable
private fun SwitchListItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    ListItem(
        modifier = modifier,
        supportingContent = supportingText,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        },
        content = headline,
    )
}

@Composable
private fun getLeadTimeLabel(leadTime: ReminderLeadTime): String =
    when (leadTime) {
        ReminderLeadTime.Five -> stringResource(R.string.settings_lead_time_5min)
        ReminderLeadTime.Ten -> stringResource(R.string.settings_lead_time_10min)
        ReminderLeadTime.Fifteen -> stringResource(R.string.settings_lead_time_15min)
        ReminderLeadTime.Thirty -> stringResource(R.string.settings_lead_time_30min)
    }

@Composable
private fun getChannelDisplayName(channel: NotificationChannelSpec): String =
    when (channel) {
        NotificationChannelSpec.SessionReminders -> stringResource(NotificationsR.string.channel_session_reminders)
        NotificationChannelSpec.ScheduleChanges -> stringResource(NotificationsR.string.channel_schedule_changes)
        NotificationChannelSpec.Announcements -> stringResource(NotificationsR.string.channel_announcements)
        NotificationChannelSpec.SocialFeed -> stringResource(NotificationsR.string.channel_social_feed)
        NotificationChannelSpec.Milestones -> stringResource(NotificationsR.string.channel_milestones)
        NotificationChannelSpec.Sync -> stringResource(NotificationsR.string.channel_sync)
    }