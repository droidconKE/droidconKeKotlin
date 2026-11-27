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
package com.android254.presentation.home.summary

import androidx.compose.runtime.Immutable
import com.google.firebase.ai.annotations.Generable
import com.google.firebase.ai.annotations.Guide
import kotlinx.serialization.Serializable

@Generable
@Serializable
data class StarredSummaryOutput(
    @Guide(description = "One or two sentences describing what the attendee's starred sessions are about")
    var overview: String = "",
    @Guide(description = "Up to three short topic names that recur across the sessions")
    var themes: List<String> = emptyList(),
) {
    companion object
}

@Immutable
data class StarredSummary(
    val overview: String,
    val themes: List<String>,
)

fun StarredSummaryOutput.toSummary() = StarredSummary(overview.trim(), themes.map(String::trim).filter(String::isNotEmpty))

@Immutable
sealed interface StarredSummaryState {
    data object Hidden : StarredSummaryState

    data object Loading : StarredSummaryState

    data object NoStarred : StarredSummaryState

    data object Unavailable : StarredSummaryState

    data object Failed : StarredSummaryState

    @Immutable
    data class Ready(
        val summary: StarredSummary,
        val source: SummarySource,
    ) : StarredSummaryState
}