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
package ke.droidcon.kotlin.core.notifications.reminders

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.android254.domain.models.Session
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Singleton
class SessionReminderScheduler
    @Inject
    constructor(
        private val workManager: WorkManager,
        private val preferencesStore: NotificationPreferencesStore,
        private val clock: Clock,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) {
        /**
         * Reminders already due are left alone: they may be running in the process that is
         * calling this, and replacing them would cancel the notification being posted. A session
         * is reminded once per start time, so a shorter lead time does not repeat a reminder.
         */
        suspend fun rescheduleAll(starredSessions: List<Session>) {
            val prefs = preferencesStore.preferences.first()

            if (!prefs.isChannelEnabled(NotificationChannelSpec.SessionReminders)) {
                workManager.cancelAllWorkByTag(TAG_SESSION_REMINDER)
                return
            }

            val leadTime = prefs.reminderLeadTimeMinutes.minutes
            val now = clock.now()
            val reminders = reminderWork()

            val starredIds = starredSessions.mapTo(mutableSetOf()) { it.remoteId }
            reminders
                .filter { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.BLOCKED }
                .mapNotNull(::sessionIdOf)
                .filterNot { it in starredIds }
                .forEach { workManager.cancelUniqueWork(workName(it)) }

            val shown =
                reminders
                    .filter { it.state == WorkInfo.State.SUCCEEDED }
                    .mapNotNullTo(mutableSetOf()) { info ->
                        sessionIdOf(info)?.let { it to info.outputData.getLong(SessionReminderWorker.KEY_REMINDED_START, 0L) }
                    }

            var scheduled = 0
            starredSessions.forEach { session ->
                val start = session.startInstant ?: return@forEach
                val fireAt = start - leadTime
                if (fireAt <= now || (session.remoteId to start.toEpochMilliseconds()) in shown) return@forEach

                workManager.enqueueUniqueWork(
                    workName(session.remoteId),
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<SessionReminderWorker>()
                        .setInitialDelay((fireAt - now).toJavaDuration())
                        .addTag(TAG_SESSION_REMINDER)
                        .addTag(sessionTag(session.remoteId))
                        .setInputData(
                            workDataOf(
                                SessionReminderWorker.KEY_SESSION_ID to session.remoteId,
                                SessionReminderWorker.KEY_TITLE to session.title,
                                SessionReminderWorker.KEY_ROOM to session.roomList.joinToString(", "),
                                SessionReminderWorker.KEY_SPEAKERS to session.speakers.joinToString { it.name },
                                SessionReminderWorker.KEY_SESSION_START to start.toEpochMilliseconds(),
                            ),
                        ).build(),
                )
                scheduled++
            }

            Timber.d("Scheduled $scheduled reminders")
        }

        private suspend fun reminderWork(): List<WorkInfo> =
            withContext(ioDispatcher) {
                workManager.getWorkInfosByTag(TAG_SESSION_REMINDER).get()
            }

        private fun sessionIdOf(info: WorkInfo): String? = info.tags.firstOrNull { it.startsWith(SESSION_TAG_PREFIX) }?.removePrefix(SESSION_TAG_PREFIX)

        private fun workName(sessionId: String) = "reminder_$sessionId"

        private fun sessionTag(sessionId: String) = "$SESSION_TAG_PREFIX$sessionId"

        companion object {
            private const val TAG_SESSION_REMINDER = "session_reminder"
            private const val SESSION_TAG_PREFIX = "session_reminder_for:"
        }
    }