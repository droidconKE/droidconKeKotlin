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
package com.android254.presentation.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.MicExternalOn
import androidx.compose.ui.graphics.Color
import com.android254.presentation.common.stepper.Intensity
import com.android254.presentation.common.stepper.VerticalStep

data class SessionPresentationModel(
    val id: String,
    val title: String,
    val description: String,
    val venue: String,
    val startTime: String,
    val endTime: String,
    val amOrPm: String,
    val isStarred: Boolean,
    val format: String,
    val level: String,
    val startDate: String,
    val endDate: String,
    val remoteId: String,
    val isService: Boolean = false,
    val sessionStatus: SessionStatus = SessionStatus.Upcoming,
    val sessionImage: String = "",
    val eventDay: String,
    val speakers: List<SessionSpeakersPresentationModel>,
) {
    // The venue accent colour used to live here as `val color`, mapped from the raw chai
    // palette. A data class cannot read MaterialTheme, so those colours could never
    // respond to dark mode. The mapping now lives in `chai.colors.venueAccentColor`,
    // which is a composable and also handles sessions listed in more than one room.

    val isServiceSession = isService && speakers.isEmpty()
    val isKeynote = format.contains("Keynote", ignoreCase = true)
    val isWorkshop = format.contains("Workshop", ignoreCase = true)
    val isTalk = format.contains("Talk", ignoreCase = true)
    val isSession = format.contains("Session", ignoreCase = true)

    val icon =
        when {
            isWorkshop -> Icons.Default.Build
            isKeynote -> Icons.Default.MicExternalOn
            isTalk || isSession -> Icons.Default.CoPresent
            isServiceSession -> Icons.Default.Coffee
            else -> Icons.Default.CoPresent
        }

    /**
     * Builds the stepper representation of this session.
     *
     * Takes [accent] rather than deriving it, so the colour comes from a composable that
     * can read the theme. This was a `val` computing its own colour from the raw palette.
     */
    fun verticalStep(accent: Color): VerticalStep<SessionPresentationModel> =
        VerticalStep(
            id = this.id,
            color = accent,
            intensity = sessionStatus.toIntensity(),
            icon = icon,
            data = this,
        )
}

data class SessionSpeakersPresentationModel(
    val name: String,
    val speakerImage: String,
    val twitterHandle: String,
)

enum class SessionStatus {
    Past,
    Ongoing,
    Upcoming,
}

fun SessionStatus.toIntensity(): Intensity {
    return when (this) {
        SessionStatus.Upcoming -> Intensity.Medium
        SessionStatus.Ongoing -> Intensity.High
        SessionStatus.Past -> Intensity.Low
    }
}