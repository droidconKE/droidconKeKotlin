/*
 * Copyright 2023 DroidconKE
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
package ke.droidcon.kotlin.core.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Whether the app may post notifications, behind an interface so view models stay JVM-testable. */
interface NotificationPermission {
    /** True on API 33+ when POST_NOTIFICATIONS is not granted. */
    fun needsRuntimeRequest(): Boolean

    /** NotificationManagerCompat.areNotificationsEnabled(), any API. */
    fun areNotificationsEnabled(): Boolean

    /** The user switched this channel off in system settings. */
    fun isChannelBlocked(channel: NotificationChannelSpec): Boolean
}

class AndroidNotificationPermission
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : NotificationPermission {
        override fun needsRuntimeRequest(): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED

        override fun areNotificationsEnabled(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

        override fun isChannelBlocked(channel: NotificationChannelSpec): Boolean =
            NotificationManagerCompat.from(context).getNotificationChannel(channel.id)?.importance ==
                NotificationManagerCompat.IMPORTANCE_NONE
    }