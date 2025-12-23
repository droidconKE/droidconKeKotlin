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
import androidx.navigation3.runtime.NavKey
import ke.droidcon.kotlin.presentation.R
import kotlinx.serialization.Serializable

@Serializable
sealed class Screens(
    @DrawableRes var icon: Int,
    var title: String,
): NavKey {
    @Serializable
    object Home : Screens(R.drawable.home_icon, "Home")

    @Serializable
    object Feed : Screens(R.drawable.feed_icon, "Feed")

    @Serializable
    object Sessions : Screens(R.drawable.sessions_icon, "Sessions")

    @Serializable
    object About : Screens(R.drawable.about_icon, "About")

    @Serializable
    object Speakers : Screens(R.drawable.droidcon_icon, "Speakers")

    @Serializable
    object FeedBack : Screens(R.drawable.droidcon_icon, "FeedBack")

    @Serializable
    class SessionDetails(val sessionId: String) :
        Screens(R.drawable.droidcon_icon, "Session Details")

    @Serializable
    class SpeakerDetails(val speakerName: String) :
        Screens(R.drawable.droidcon_icon, "Speaker Details")
}

val bottomNavigationDestinations =
    setOf(
        Screens.Home,
        Screens.Feed,
        Screens.Sessions,
        Screens.About,
    )