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
package com.android254.data.flags

import com.android254.domain.flags.FeatureFlags
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import javax.inject.Inject

class RemoteFeatureFlags
    @Inject
    constructor(
        private val remoteFeatureToggle: RemoteFeatureToggle,
    ) : FeatureFlags {
        override suspend fun isCubeHeroEnabled(): Boolean {
            remoteFeatureToggle.awaitDefaults()
            return remoteFeatureToggle.getBoolean(CUBE_HERO_ENABLED)
        }

        override suspend fun isStarredSummaryEnabled(): Boolean {
            remoteFeatureToggle.awaitDefaults()
            return remoteFeatureToggle.getBoolean(STARRED_SUMMARY_ENABLED)
        }

        override suspend fun isAskConferenceEnabled(): Boolean {
            remoteFeatureToggle.awaitDefaults()
            return remoteFeatureToggle.getBoolean(ASK_CONFERENCE_ENABLED)
        }

        override suspend fun isStarSessionFunctionEnabled(): Boolean {
            remoteFeatureToggle.awaitDefaults()
            return remoteFeatureToggle.getBoolean(STAR_SESSION_FUNCTION_ENABLED)
        }

        override suspend fun askConferenceModel(): String {
            remoteFeatureToggle.awaitDefaults()
            return remoteFeatureToggle.getString(ASK_CONFERENCE_MODEL)
        }

        private companion object {
            const val CUBE_HERO_ENABLED = "cube_hero_enabled"
            const val STARRED_SUMMARY_ENABLED = "starred_summary_enabled"
            const val ASK_CONFERENCE_ENABLED = "ask_conference_enabled"
            const val ASK_CONFERENCE_MODEL = "ask_conference_model"
            const val STAR_SESSION_FUNCTION_ENABLED = "star_session_function_enabled"
        }
    }