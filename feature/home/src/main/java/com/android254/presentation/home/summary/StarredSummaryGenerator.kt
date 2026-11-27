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
import com.google.firebase.Firebase
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.ai
import com.google.firebase.ai.ondevice.FirebaseAIOnDevice
import com.google.firebase.ai.ondevice.OnDeviceModelStatus
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

enum class SummarySource { OnDevice, Cloud }

sealed interface SummaryResult {
    data class Success(
        val summary: StarredSummary,
        val source: SummarySource,
    ) : SummaryResult

    data object Unavailable : SummaryResult

    data object Failed : SummaryResult
}

fun interface StarredSummaryGenerator {
    suspend fun summarise(sessions: List<Session>): SummaryResult
}

@OptIn(PublicPreviewAPI::class)
class HybridStarredSummaryGenerator
    @Inject
    constructor() : StarredSummaryGenerator {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun summarise(sessions: List<Session>): SummaryResult =
            try {
                startNanoDownloadIfNeeded()
                val model =
                    Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                        modelName = CLOUD_MODEL,
                        onDeviceConfig = OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE),
                    )
                val response = model.generateObject(StarredSummaryOutput.firebaseAISchema(), prompt(sessions))
                val source = if (response.response.inferenceSource == InferenceSource.ON_DEVICE) SummarySource.OnDevice else SummarySource.Cloud
                response.getObject()?.let { SummaryResult.Success(it.toSummary(), source) } ?: SummaryResult.Failed
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                if (e.isNetworkFailure()) {
                    Timber.i("Starred summary unavailable offline: %s", e.javaClass.simpleName)
                    SummaryResult.Unavailable
                } else {
                    Timber.w(e, "Starred summary failed")
                    SummaryResult.Failed
                }
            }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun startNanoDownloadIfNeeded() {
            val status =
                try {
                    FirebaseAIOnDevice.checkStatus()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Timber.w(e, "Gemini Nano status check failed; summarising in the cloud")
                    return
                }
            Timber.d("Gemini Nano status=%s", status)
            if (status == OnDeviceModelStatus.DOWNLOADABLE && downloading.compareAndSet(false, true)) {
                downloadScope.launch {
                    FirebaseAIOnDevice
                        .download()
                        .catch { Timber.w(it, "Gemini Nano download failed") }
                        .onCompletion { downloading.set(false) }
                        .collect { Timber.d("Gemini Nano download: %s", it) }
                }
            }
        }

        private fun Throwable.isNetworkFailure(): Boolean = generateSequence(this) { it.cause }.any { it is IOException }

        private fun prompt(sessions: List<Session>) =
            buildString {
                appendLine("Summarise the conference sessions I have starred. Write to me as \"you\". Be brief and concrete.")
                sessions.take(MAX_SESSIONS).forEach { appendLine("- ${it.title}: ${it.description.take(MAX_DESCRIPTION)}") }
            }

        private companion object {
            val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val downloading = AtomicBoolean(false)
            const val CLOUD_MODEL = "gemini-3.5-flash"
            const val MAX_SESSIONS = 20
            const val MAX_DESCRIPTION = 200
        }
    }