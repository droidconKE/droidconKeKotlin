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
package com.android254.presentation.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android254.presentation.about.view.OrganizingTeamSection
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.components.OrganizedBySection
import com.android254.presentation.models.OrganizingTeamMember
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class OrganisersScreenshotTest : ChaiScreenshotTest() {
    override fun placeholderImage() = wideLogoImage()

    @Test
    fun `organisers across form factors`() =
        captureFormFactors("form_factors/organisers") {
            Column(Modifier.fillMaxWidth().readablePaneWidth()) {
                OrganizingTeamSection(
                    organizingTeam =
                        List(8) { index ->
                            OrganizingTeamMember(name = "Member ${index + 1}", desc = "Organiser", image = "")
                        }.toImmutableList(),
                    onClickMember = {},
                )
                Spacer(Modifier.height(40.dp))
                OrganizedBySection(
                    organizationLogos = List(6) { index -> "https://example.com/logo-$index.png" }.toImmutableList(),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
}