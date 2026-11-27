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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R

/** One session from the schedule, with a Star button that sends [STAR_EVENT] back through A2UI. */
internal object SessionCardComponent : A2uiComponent {
    const val STAR_EVENT = "starSession"
    const val SESSION_ID_KEY = "sessionId"

    private val sessionId =
        A2uiProperty.string(SESSION_ID_KEY, required = true, description = "The session's id, copied exactly from the schedule.")
    private val title = A2uiProperty.dynamicString("title", required = true, description = "The session title.")
    private val time = A2uiProperty.dynamicString("time", description = "Day and time, such as \"Day 2, 14:00-14:40\".")
    private val room = A2uiProperty.dynamicString("room", description = "The room or rooms.")
    private val speakers = A2uiProperty.dynamicString("speakers", description = "Speaker names, comma-separated.")
    private val starred = A2uiProperty.boolean("starred", description = "True if the user already starred this session.")

    override val name = "SessionCard"
    override val description = "One conference session, with a Star button that adds it to the user's sessions."
    override val properties = listOf(sessionId, title, time, room, speakers, starred)

    @Composable
    override fun A2uiComponentScope.Content(
        properties: A2uiComponentProperties,
        modifier: Modifier,
    ) {
        val id = properties[sessionId] ?: return
        val details = listOfNotNull(properties.bind(time), properties.bind(room)).filter(String::isNotBlank)
        val speakerNames = properties.bind(speakers).orEmpty()
        var isStarred by rememberSaveable(id) { mutableStateOf(properties[starred] ?: false) }
        Card(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = properties.bind(title).orEmpty(), style = MaterialTheme.typography.titleMedium)
                if (details.isNotEmpty()) {
                    Text(text = details.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                }
                if (speakerNames.isNotBlank()) {
                    Text(text = speakerNames, style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.weight(1f))
                    FilledTonalButton(
                        enabled = !isStarred,
                        onClick = {
                            isStarred = true
                            dispatchAction(starAction(id))
                        },
                    ) {
                        Icon(
                            imageVector = if (isStarred) ChaiIcons.StarRounded else ChaiIcons.StarOutlineRounded,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(if (isStarred) R.string.ask_session_starred else R.string.ask_session_star))
                    }
                }
            }
        }
    }

    fun starAction(id: String): Map<String, Any?> = mapOf("event" to mapOf("name" to STAR_EVENT, "context" to mapOf(SESSION_ID_KEY to id)))
}