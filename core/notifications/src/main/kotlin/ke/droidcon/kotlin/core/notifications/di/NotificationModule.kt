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
package ke.droidcon.kotlin.core.notifications.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ke.droidcon.kotlin.core.notifications.AndroidNotificationPermission
import ke.droidcon.kotlin.core.notifications.NotificationPermission
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesImpl
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import javax.inject.Singleton

/**
 * Hilt DI module for notification-related dependencies.
 *
 * Provides singleton instances of the notification preferences store.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    @Singleton
    abstract fun bindNotificationPreferencesStore(
        impl: NotificationPreferencesImpl,
    ): NotificationPreferencesStore

    @Binds
    abstract fun bindNotificationPermission(impl: AndroidNotificationPermission): NotificationPermission
}