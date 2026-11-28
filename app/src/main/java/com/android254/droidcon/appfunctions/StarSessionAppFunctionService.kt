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
package com.android254.droidcon.appfunctions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.android254.domain.repos.SessionsRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** The session that was starred. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class StarredSession(
    /** The ID of the starred session. */
    val sessionId: String,
    /** The title of the starred session. */
    val title: String,
)

/** Exposes the DroidconKE session actions to assistants. */
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@AndroidEntryPoint
@AppFunctionServiceEntryPoint(
    serviceName = "StarSessionAppFunctionService",
    appFunctionXmlFileName = "star_session_app_function_service",
)
abstract class BaseStarSessionAppFunctionService : AppFunctionService() {
    @Inject internal lateinit var sessionsRepo: SessionsRepo

    /**
     * Stars a conference session so it appears in the user's starred sessions.
     *
     * @param sessionId The ID of the session to star, as used by the app for session details.
     * @return The session that was starred. Starring an already starred session has no further effect.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun starSession(sessionId: String): StarredSession {
        if (sessionId.isBlank()) throw AppFunctionInvalidArgumentException("sessionId must not be empty.")
        val session =
            sessionsRepo.fetchSessionById(sessionId).first()
                ?: throw AppFunctionElementNotFoundException("No session has the ID $sessionId.")
        sessionsRepo.bookmarkSession(session.id)
        return StarredSession(sessionId = session.id, title = session.title)
    }
}