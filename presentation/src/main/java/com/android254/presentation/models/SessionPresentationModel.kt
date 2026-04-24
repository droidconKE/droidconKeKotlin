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
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.MicExternalOn
import com.android254.presentation.common.stepper.VerticalStep
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.atoms.ChaiTeal

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
    val sessionImage: String = "",
    val eventDay: String,
    val speakers: List<SessionSpeakersPresentationModel>,
) {
    val color = when (venue) {
        "Sapphire,Opal" -> ChaiBlue
        "Opal" -> ChaiRed
        "Sapphire" -> ChaiTeal
        else -> ChaiBlue
    }
    val icon = when (format) {
        "Workshop" -> Icons.Outlined.Build
        "Lightning talk" -> Icons.Outlined.MicExternalOn
        "Session" -> Icons.Outlined.CoPresent
        else -> Icons.Outlined.CoPresent
    }

    val isServiceSession = isService && speakers.isEmpty()
    val isKeynote = format.contains("Keynote", ignoreCase = true)
    val isWorkshop = format.contains("Workshop", ignoreCase = true)
    val isTalk = format.contains("Talk", ignoreCase = true)

    val toVerticalStep: VerticalStep<SessionPresentationModel> = VerticalStep(
        id = this.id,
        color = color,
        icon = icon,
        data = this
    )
}

data class SessionSpeakersPresentationModel(
    val name: String,
    val speakerImage: String,
    val twitterHandle: String,
)