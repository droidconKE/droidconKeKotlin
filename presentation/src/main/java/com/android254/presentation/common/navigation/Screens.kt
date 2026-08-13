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
package com.android254.presentation.common.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Destinations in the app.
 *
 * Keys are immutable data and nothing else — they are serialized to `SavedState`. Display
 * metadata lives in [TopLevelDestination].
 */
@Serializable
sealed interface Screens : NavKey {
    @Serializable
    data object Home : Screens

    @Serializable
    data object Feed : Screens

    @Serializable
    data object Sessions : Screens

    @Serializable
    data object About : Screens

    @Serializable
    data object Speakers : Screens

    @Serializable
    data object FeedBack : Screens

    @Serializable
    data class SessionDetails(val sessionId: String) : Screens

    @Serializable
    data class SpeakerDetails(val speakerName: String) : Screens
}

val bottomNavigationRoutes: List<Screens> = TopLevelDestination.routes

val bottomNavigationSet: Set<Screens> = TopLevelDestination.routeSet