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

import androidx.navigation3.runtime.NavKey

/** The list this route is the detail of. One declaration for two decisions that must not drift. */
val Screens.listPaneRoute: Screens?
    get() =
        when (this) {
            is Screens.SessionDetails -> Screens.Sessions
            is Screens.SpeakerDetails -> Screens.Speakers
            else -> null
        }

/**
 * Whether the navigation area should be visible on [route]. A detail hides it only while
 * full-screen — the Material guidance is explicit that it must return once the detail is a pane.
 */
fun shouldShowNavigation(
    route: NavKey,
    isMultiPaneWindow: Boolean,
): Boolean =
    when {
        route == Screens.FeedBack -> false
        route is Screens && route.listPaneRoute != null -> isMultiPaneWindow
        else -> true
    }

/**
 * Whether the standing "happening now" pane belongs beside [route]. A detail keeps that column
 * for itself, and [hasLiveSessions] stops one being held open for an empty state.
 */
fun shouldShowSupportingPane(
    route: NavKey,
    isMultiPaneWindow: Boolean,
    hasLiveSessions: Boolean,
): Boolean =
    isMultiPaneWindow &&
        hasLiveSessions &&
        shouldShowNavigation(route, isMultiPaneWindow) &&
        (route as? Screens)?.listPaneRoute == null