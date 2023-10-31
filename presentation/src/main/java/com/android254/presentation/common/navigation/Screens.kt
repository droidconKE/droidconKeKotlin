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
package com.android254.presentation.common.navigation

import androidx.annotation.DrawableRes
import ke.droidcon.kotlin.presentation.R

sealed class Screens(var route: String, @DrawableRes var icon: Int, var title: String) {
    object Home : Screens("/home", R.drawable.home_icon, "Home")
    object Feed : Screens("/feed", R.drawable.feed_icon, "Feed")
    object Sessions : Screens("/sessions", R.drawable.sessions_icon, "Sessions")
    object About : Screens("/about", R.drawable.about_icon, "About")
    object Speakers : Screens("/speakers", R.drawable.droidcon_icon, "Speakers")
    object FeedBack : Screens("/feedback", R.drawable.droidcon_icon, "FeedBack")
    object SessionDetails :
        Screens("/sessionDetails/{sessionId}", R.drawable.droidcon_icon, "Session Details") {
        const val sessionIdNavigationArgument = "sessionId"
    }

    object SpeakerDetails :
        Screens("/speaker_details/{speakerName}", R.drawable.droidcon_icon, "Speaker Details")
}

val bottomNavigationDestinations = listOf(
    Screens.Home,
    Screens.Feed,
    Screens.Sessions,
    Screens.About
)