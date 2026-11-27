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

import androidx.a2ui.compose.runtime.A2uiMessageParser
import androidx.a2ui.compose.ui.A2uiMessageProcessor
import androidx.a2ui.compose.ui.toJsonSchemaString
import androidx.a2ui.model.processor.A2uiActionInterceptor
import androidx.a2ui.model.processor.A2uiSurfaceModel
import androidx.a2ui.model.processor.processInput
import androidx.a2ui.model.protocol.A2uiClientErrorMessage
import androidx.a2ui.model.protocol.A2uiEventAction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.flags.FeatureFlags
import com.android254.domain.repos.SessionsRepo
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.sessions.mappers.toPresentationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Clock

/** Holds the question, the agent's call and the A2UI surfaces it produces. */
@HiltViewModel
class AskViewModel
    @Inject
    constructor(
        private val sessionsRepo: SessionsRepo,
        private val agent: AskAgent,
        private val featureFlags: FeatureFlags,
        private val clock: Clock,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val parser = A2uiMessageParser()

        @Volatile
        private var scheduleIds: Set<String> = emptySet()
        private val starInterceptor =
            A2uiActionInterceptor { action ->
                val sessionId = (action as? A2uiEventAction)?.starredSessionId() ?: return@A2uiActionInterceptor action
                if (sessionId in scheduleIds) {
                    viewModelScope.launch {
                        val starred = sessionsRepo.fetchSessionById(sessionId).first()?.isBookmarked == true
                        if (starred) sessionsRepo.unBookmarkSession(sessionId) else sessionsRepo.bookmarkSession(sessionId)
                    }
                } else {
                    Timber.w("Ignored a star for a session id the schedule doesn't have: %s", sessionId)
                }
                null
            }
        private val processor = A2uiMessageProcessor(catalogs = listOf(AskCatalog.catalog), interceptors = listOf(starInterceptor))
        private val catalogSchema by lazy { AskCatalog.catalog.toJsonSchemaString() }

        val surfaces: StateFlow<List<A2uiSurfaceModel>> = processor.activeSurfaces

        /** Every session the answer's cards can show, live from the database so stars stay current. */
        val sessions: StateFlow<ImmutableMap<String, SessionPresentationModel>> =
            sessionsRepo
                .fetchSessions()
                .map { sessions ->
                    val now = clock.now()
                    sessions.associate { it.id to it.toPresentationModel(now) }.toImmutableMap()
                }.flowOn(ioDispatcher)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), persistentMapOf())

        private val _state = MutableStateFlow(AskState())
        val state: StateFlow<AskState> = _state.asStateFlow()

        init {
            viewModelScope.launch { processor.collectMessages() }
            viewModelScope.launch {
                processor.outboundEvents.collect { event ->
                    Timber.i("A2UI event for the agent: %s", event)
                    if (event is A2uiClientErrorMessage) _state.update { it.copy(failed = true) }
                }
            }
        }

        fun onQuestionChange(question: String) {
            _state.update { it.copy(question = question.take(MAX_QUESTION_LENGTH)) }
        }

        fun ask() {
            val question = _state.value.question.trim()
            if (question.isEmpty() || _state.value.isAsking) return
            _state.update { it.copy(isAsking = true, failed = false) }
            viewModelScope.launch {
                try {
                    if (!featureFlags.isAskConferenceEnabled()) {
                        _state.update { it.copy(isAsking = false, failed = true) }
                        return@launch
                    }
                    val messages =
                        withContext(ioDispatcher) {
                            val sessions = sessionsRepo.fetchSessions().first()
                            scheduleIds = sessions.mapTo(HashSet()) { it.id }
                            val schedule = AskPrompt.schedule(sessions)
                            val started = System.currentTimeMillis()
                            val reply = agent.ask(question, schedule, catalogSchema)
                            Timber.i(
                                "Ask the conference: %d ms, schedule %d chars, schema %d chars, reply %d chars",
                                System.currentTimeMillis() - started,
                                schedule.length,
                                catalogSchema.length,
                                reply.length,
                            )
                            splitA2uiMessages(reply)
                        }
                    surfaces.value.forEach { processor.processInput(parser, AskPrompt.deleteSurface(it.id)) }
                    messages.forEach { processor.processInput(parser, it) }
                    _state.update { it.copy(isAsking = false, failed = messages.isEmpty()) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.w(e, "Ask the conference failed")
                    _state.update { it.copy(isAsking = false, failed = true) }
                }
            }
        }
    }

private const val MAX_QUESTION_LENGTH = 300
private const val STOP_TIMEOUT_MILLIS = 5_000L

data class AskState(
    val question: String = "",
    val isAsking: Boolean = false,
    val failed: Boolean = false,
)

internal fun A2uiEventAction.starredSessionId(): String? = if (eventName == SessionCardComponent.STAR_EVENT) context[SessionCardComponent.SESSION_ID_KEY] as? String else null