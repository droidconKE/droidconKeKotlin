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
package ke.droidcon.kotlin.datasource.remote

object Constants {
    // const val DEV_BASE_URL = "https://dev.droidcon.co.ke/api/v1"
    const val PROD_BASE_URL = "https://api.droidcon.co.ke/v1"
    const val DEV_BASE_URL = "https://api.droidcon.co.ke/v1"
    private const val EVENT_SLUG = "droidconke-2023-137"
    private const val ORG_SLUG = "droidcon-ke-645"
    const val EVENT_PROD_BASE_URL = "$PROD_BASE_URL/events/$EVENT_SLUG"
    const val EVENT_DEV_BASE_URL = "$DEV_BASE_URL/events/$EVENT_SLUG"
    const val ORG_PROD_BASE_URL = "$PROD_BASE_URL/organizers/$ORG_SLUG"
    const val ORG_DEV_BASE_URL = "$DEV_BASE_URL/organizers/$ORG_SLUG"
}