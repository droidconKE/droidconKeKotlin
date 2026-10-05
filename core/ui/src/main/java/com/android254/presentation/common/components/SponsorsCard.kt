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
package com.android254.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.SponsorPresentationModel
import com.droidconke.chai.isDarkTheme
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SponsorsCard(
    sponsors: ImmutableList<SponsorPresentationModel>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("sponsors_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.sponsors_title),
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() },
        )
        val (platinum, others) = sponsors.partition { it.sponsorType.equals("platinum", ignoreCase = true) }
        val isDark = MaterialTheme.isDarkTheme
        platinum.forEach { sponsor ->
            LogoTile(
                logos = sponsor.logosFor(isDark),
                name = sponsor.name,
                logoHeight = 56.dp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        EvenGrid(items = others.toImmutableList(), columns = 2) { sponsor, cellModifier ->
            LogoTile(logos = sponsor.logosFor(isDark), name = sponsor.name, modifier = cellModifier)
        }
    }
}

private fun SponsorPresentationModel.logosFor(isDark: Boolean) = if (isDark) persistentListOf(logo.replace(".png", "-dark.png"), logo) else persistentListOf(logo)