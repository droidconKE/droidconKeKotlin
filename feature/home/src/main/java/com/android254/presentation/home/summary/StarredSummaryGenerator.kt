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
package com.android254.presentation.home.summary

import com.android254.domain.models.Session
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateTypedContentRequest
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface SummaryResult {
    data class Success(
        val summary: StarredSummary,
    ) : SummaryResult

    data object Unavailable : SummaryResult

    data object Failed : SummaryResult
}

fun interface StarredSummaryGenerator {
    suspend fun summarise(sessions: List<Session>): SummaryResult
}

class GeminiNanoStarredSummaryGenerator
    @Inject
    constructor() : StarredSummaryGenerator {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun summarise(sessions: List<Session>): SummaryResult {
            val model = runCatching { Generation.getClient() }.getOrNull()
            if (model == null) {
                Timber.w("Starred summary unavailable: Generation.getClient() failed")
                return SummaryResult.Unavailable
            }
            return try {
                val status = model.checkStatus()
                if (status == FeatureStatus.UNAVAILABLE) {
                    Timber.w("Starred summary unavailable: Gemini Nano status=%s", status)
                    return SummaryResult.Unavailable
                }
                if (status != FeatureStatus.AVAILABLE) {
                    Timber.i("Starred summary not ready: Gemini Nano status=%s", status)
                    return SummaryResult.Failed
                }
                if (!model.isStructuredOutputFeatureAvailable()) {
                    Timber.w("Starred summary unavailable: structured output not supported")
                    return SummaryResult.Unavailable
                }
                val request = GenerateContentRequest.Builder(TextPart(prompt(sessions))).build()
                val response = model.generateContent(generateTypedContentRequest(request, StarredSummaryOutput::class))
                response.candidates
                    .firstOrNull()
                    ?.response
                    ?.toSummary()
                    ?.let(SummaryResult::Success)
                    ?: SummaryResult.Failed.also { Timber.w("Starred summary failed: empty response") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Timber.w(e, "Starred summary failed")
                SummaryResult.Failed
            } finally {
                model.close()
            }
        }

        private fun prompt(sessions: List<Session>) =
            buildString {
                appendLine("Summarise what this conference attendee has starred. Be brief and concrete.")
                sessions.take(MAX_SESSIONS).forEach { appendLine("- ${it.title}: ${it.description.take(MAX_DESCRIPTION)}") }
            }

        private companion object {
            const val MAX_SESSIONS = 20
            const val MAX_DESCRIPTION = 200
        }
    }