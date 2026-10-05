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
package com.android254.presentation.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.components.SponsorsCard
import com.android254.presentation.models.SponsorPresentationModel
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

/** The full home capture stops above the sponsors, so they get one of their own. */
class SponsorsScreenshotTest : ChaiScreenshotTest() {
    override fun placeholderImage() = wideLogoImage()

    @Test
    fun `sponsors across form factors`() =
        captureFormFactors("form_factors/sponsors") {
            SponsorsCard(
                sponsors =
                    listOf("platinum", "gold", "silver", "bronze")
                        .map { tier ->
                            SponsorPresentationModel(name = tier, link = "", logo = "https://example.com/$tier.png", sponsorType = tier)
                        }.toImmutableList(),
                modifier = Modifier.fillMaxWidth().readablePaneWidth().padding(horizontal = 20.dp),
            )
        }
}