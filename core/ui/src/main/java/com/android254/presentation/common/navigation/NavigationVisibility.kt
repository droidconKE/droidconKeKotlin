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

/**
 * The list this route is the detail of, or null if it is not a detail of anything.
 *
 * One declaration for two decisions that must not drift: which pairs of entries form a
 * list-detail scene, and whether a detail is currently full-screen — which is the same question
 * as whether its list is beside it.
 */
val Screens.listPaneRoute: Screens?
    get() =
        when (this) {
            is Screens.SessionDetails -> Screens.Sessions
            is Screens.SpeakerDetails -> Screens.Speakers
            else -> null
        }

/**
 * Whether the navigation area should be visible on [route].
 *
 * Three routes used to hide the bottom bar by calling back into the composition root as a side
 * effect of composing. Two of them hide it because they are full-screen details, and the
 * Material guidance is explicit that full-screen mode must be switched off once the detail is a
 * pane instead — so on a window wide enough to hold both panes, they stop hiding it. Feedback is
 * not a detail of anything; it is a focused task that owns the window at every size.
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
 * Whether the standing "happening now" pane should be beside the content on [route].
 *
 * Only where there is a second column to spare and nothing better to put in it. A detail keeps
 * that column for itself, and the routes that hide the navigation are focused tasks that should
 * not gain a sidebar.
 */
fun shouldShowSupportingPane(
    route: NavKey,
    isMultiPaneWindow: Boolean,
): Boolean =
    isMultiPaneWindow &&
        shouldShowNavigation(route, isMultiPaneWindow) &&
        (route as? Screens)?.listPaneRoute == null