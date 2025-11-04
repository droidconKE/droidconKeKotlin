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
package com.android254.droidconKE2023.crashlytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ke.droidcon.kotlin.BuildConfig
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        if (BuildConfig.DEBUG) {
            crashlytics.setCrashlyticsCollectionEnabled(false)
            return
        }

        crashlytics.setCustomKey(CRASHLYTICS_KEY_PRIORITY, priority)
        if (tag != null) {
            crashlytics.setCustomKey(CRASHLYTICS_KEY_TAG, tag)
        }
        crashlytics.setCustomKey(CRASHLYTICS_KEY_MESSAGE, message)

        if (t == null) {
            crashlytics.recordException(Exception(message))
        } else {
            crashlytics.recordException(t)
        }
    }

    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }
}