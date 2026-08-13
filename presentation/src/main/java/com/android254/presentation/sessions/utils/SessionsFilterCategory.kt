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
package com.android254.presentation.sessions.utils

import ke.droidcon.kotlin.presentation.R

/**
 * Facets a session list can be filtered by.
 *
 * `Topic` used to be a member here. Nothing could ever populate it: the filter panel
 * emitted no topic options and the API returns no topic field, so it was unreachable in
 * both directions. Removed rather than left half-wired. Reintroduce it only alongside a
 * backend field and a UI that offers it.
 */
enum class SessionsFilterCategory(val resId: Int) {
    Level(R.string.title_filter_level),
    Room(R.string.title_filter_room),
    SessionType(R.string.title_filter_session_type),
}