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
package com.android254.droidcon.app

import com.google.firebase.Firebase
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import ke.droidcon.kotlin.BuildConfig

/** Proves calls to Firebase AI Logic come from this app: Play Integrity in release, a debug token in debug. */
internal object AppCheckSetup {
    fun install() {
        Firebase.appCheck.installAppCheckProviderFactory(providerFactory())
    }

    // The debug provider is a debugImplementation dependency, so release builds can't reference it directly.
    private fun providerFactory(): AppCheckProviderFactory =
        if (BuildConfig.DEBUG) {
            Class
                .forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                .getMethod("getInstance")
                .invoke(null) as AppCheckProviderFactory
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
}