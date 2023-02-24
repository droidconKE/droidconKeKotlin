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
package com.android254.data.network.util

import com.android254.data.network.Constants.LIVE_BASE_URL
import com.android254.data.network.Constants.TEST_BASE_URL
import com.android254.droidconKE2023.data.BuildConfig

fun provideBaseUrl(): String = if (BuildConfig.DEBUG) TEST_BASE_URL else LIVE_BASE_URL