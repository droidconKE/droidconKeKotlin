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
package com.android254.presentation.common.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import ke.droidcon.kotlin.chai.R as ChaiR

/**
 * The navigation bar tabs and the metadata needed to draw them. Separate from [Screens] so
 * keys stay pure serializable data and labels stay localisable.
 */
enum class TopLevelDestination(
    val route: Screens,
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
) {
    HOME(Screens.Home, ChaiR.drawable.home_icon, R.string.nav_home),
    FEED(Screens.Feed, ChaiR.drawable.feed_icon, R.string.nav_feed),
    SESSIONS(Screens.Sessions, ChaiR.drawable.sessions_icon, R.string.nav_sessions),
    ABOUT(Screens.About, ChaiR.drawable.about_icon, R.string.nav_about),
    ;

    companion object {
        val routes: List<Screens> = entries.map { it.route }
        val routeSet: ImmutableSet<Screens> = routes.toImmutableSet()

        fun of(route: Screens): TopLevelDestination? = entries.firstOrNull { it.route == route }
    }
}