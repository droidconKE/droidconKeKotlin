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
plugins {
    alias(libs.plugins.droidconke.android.library)
    alias(libs.plugins.droidconke.android.room)
    alias(libs.plugins.droidconke.android.hilt)
    alias(libs.plugins.droidconke.android.library.firebase)
    alias(libs.plugins.droidconke.android.library.jacoco)
}

android {
    namespace = "ke.droidcon.kotlin.datasource.local"
}

dependencies {
    implementation(libs.kotlin.coroutines.datetime)
    implementation(libs.timber)
    implementation(libs.datastore)
    implementation(libs.kotlin.coroutines.android)

    implementation(libs.lifecycle.livedataKtx)

    implementation(libs.work.runtime)

    releaseImplementation(libs.chucker.release)
    debugImplementation(libs.chucker.debug)

    testImplementation(libs.app.cash.turbine.turbine)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.test.androidx.core)
    testImplementation(libs.test.robolectric)
}