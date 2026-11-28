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
package com.android254.presentation.ask

import androidx.a2ui.compose.runtime.A2uiComponentProperties
import androidx.a2ui.compose.runtime.A2uiComponentScope
import androidx.a2ui.compose.runtime.A2uiProperty
import androidx.a2ui.compose.ui.A2uiComponent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.android254.presentation.common.components.SessionsCard
import com.android254.presentation.models.SessionPresentationModel
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/** The app's sessions by id, and where a tapped card goes, for [SessionCardComponent]. */
@Immutable
internal data class AskSessionCards(
    val sessions: ImmutableMap<String, SessionPresentationModel> = persistentMapOf(),
    val onOpen: (String) -> Unit = {},
)

// A2UI renders catalog components itself, so app state can only reach them through the composition.
@Suppress("ComposeCompositionLocalUsage")
internal val LocalAskSessionCards = staticCompositionLocalOf { AskSessionCards() }

/** One session, drawn with the app's own session card; its star sends [STAR_EVENT] back through A2UI. */
internal object SessionCardComponent : A2uiComponent {
    const val STAR_EVENT = "toggleStar"
    const val SESSION_ID_KEY = "sessionId"

    private val sessionId =
        A2uiProperty.string(SESSION_ID_KEY, required = true, description = "The session's id, copied exactly from the schedule.")

    override val name = "SessionCard"
    override val description = "One conference session from the schedule, shown with its title, tags, room, time and a star button."
    override val properties = listOf(sessionId)

    @Composable
    override fun A2uiComponentScope.Content(
        properties: A2uiComponentProperties,
        modifier: Modifier,
    ) {
        val cards = LocalAskSessionCards.current
        val session = properties[sessionId]?.let(cards.sessions::get) ?: return
        SessionsCard(
            session = session,
            navigateToSessionDetails = cards.onOpen,
            onBookmark = { id -> dispatchAction(starAction(id)) },
            modifier = modifier,
        )
    }

    fun starAction(id: String): Map<String, Any?> = mapOf("event" to mapOf("name" to STAR_EVENT, "context" to mapOf(SESSION_ID_KEY to id)))
}