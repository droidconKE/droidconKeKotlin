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
package com.android254.data.network

object Constants {
    const val BASE_URL = "https://droidcon-erp.herokuapp.com/api/v1"
    const val LIVE_BASE_URL = "https://api.droidcon.co.ke/v1"
    const val EVENT_SLUG = "droidconke-2022-281"
    const val ORG_SLUG = "droidcon-ke-645"
    const val EVENT_BASE_URL = "$LIVE_BASE_URL/events/$EVENT_SLUG"
}