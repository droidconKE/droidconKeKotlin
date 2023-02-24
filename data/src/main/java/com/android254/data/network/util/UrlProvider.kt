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

import com.android254.data.network.Constants.DEV_BASE_URL
import com.android254.data.network.Constants.EVENT_DEV_BASE_URL
import com.android254.data.network.Constants.EVENT_PROD_BASE_URL
import com.android254.data.network.Constants.ORG_DEV_BASE_URL
import com.android254.data.network.Constants.ORG_PROD_BASE_URL
import com.android254.data.network.Constants.PROD_BASE_URL
import com.android254.droidconKE2023.data.BuildConfig

fun provideBaseUrl(): String = if (BuildConfig.DEBUG) DEV_BASE_URL else PROD_BASE_URL

fun provideEventBaseUrl(): String =
    if (BuildConfig.DEBUG) EVENT_DEV_BASE_URL else EVENT_PROD_BASE_URL

fun provideOrgBaseUrl(): String = if (BuildConfig.DEBUG) ORG_DEV_BASE_URL else ORG_PROD_BASE_URL