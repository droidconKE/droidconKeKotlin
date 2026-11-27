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
package com.android254.presentation.ask

import androidx.a2ui.compose.ui.A2uiCatalog
import androidx.compose.material3.a2ui.catalog.MaterialA2uiBasicCatalogV1Defaults

/** What the agent may draw: two Basic Catalog pieces and the app's own session card. */
internal object AskCatalog {
    const val ID = "https://droidcon.co.ke/a2ui/ask/v1/catalog.json"

    val catalog: A2uiCatalog =
        A2uiCatalog(
            catalogId = ID,
            components =
                listOf(
                    MaterialA2uiBasicCatalogV1Defaults.column,
                    MaterialA2uiBasicCatalogV1Defaults.text,
                    SessionCardComponent,
                ),
        )
}