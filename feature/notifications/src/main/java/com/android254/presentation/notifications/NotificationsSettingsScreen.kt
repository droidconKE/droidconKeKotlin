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

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.data.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.ui.R

enum class ReminderLeadTime(val minutes: Int) {
    FIVE(5),
    TEN(10),
    FIFTEEN(15),
    THIRTY(30),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    viewModel: NotificationsSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_notifications_title)) },
                navigationIcon = {
                    androidx.compose.material.icons.Icons.Default.ArrowBack?.let {
                        androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                            androidx.compose.material3.Icon(it, contentDescription = null)
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SwitchListItem(
                    checked = uiState.masterEnabled,
                    onCheckedChange = { viewModel.setMasterEnabled(it) },
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
                        onCheckedChange = { viewModel.setChannelEnabled(channel, it) },
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = uiState.reminderLeadTimeMinutes == leadTime.minutes,
                                    onClick = { viewModel.setReminderLeadTime(leadTime.minutes) },
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

@Composable
private fun getLeadTimeLabel(leadTime: ReminderLeadTime): String {
    return when (leadTime) {
        ReminderLeadTime.FIVE -> stringResource(R.string.settings_lead_time_5min)
        ReminderLeadTime.TEN -> stringResource(R.string.settings_lead_time_10min)
        ReminderLeadTime.FIFTEEN -> stringResource(R.string.settings_lead_time_15min)
        ReminderLeadTime.THIRTY -> stringResource(R.string.settings_lead_time_30min)
    }
}

@Composable
private fun getChannelDisplayName(channel: NotificationChannelSpec): String {
    return when (channel) {
        NotificationChannelSpec.SessionReminders -> stringResource(R.string.channel_session_reminders)
        NotificationChannelSpec.ScheduleChanges -> stringResource(R.string.channel_schedule_changes)
        NotificationChannelSpec.Announcements -> stringResource(R.string.channel_announcements)
        NotificationChannelSpec.SocialFeed -> stringResource(R.string.channel_social_feed)
        NotificationChannelSpec.Milestones -> stringResource(R.string.channel_milestones)
        NotificationChannelSpec.Sync -> stringResource(R.string.channel_sync)
    }
}
