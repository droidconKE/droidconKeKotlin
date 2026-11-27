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

import com.android254.domain.flags.FeatureFlags
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import javax.inject.Inject

/** Answers a question about the schedule with A2UI messages, as one JSON array. */
interface AskAgent {
    suspend fun ask(
        question: String,
        schedule: String,
        catalogSchema: String,
    ): String
}

class FirebaseAskAgent
    @Inject
    constructor(
        private val featureFlags: FeatureFlags,
    ) : AskAgent {
        override suspend fun ask(
            question: String,
            schedule: String,
            catalogSchema: String,
        ): String {
            val model =
                Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                    modelName = featureFlags.askConferenceModel().ifBlank { DEFAULT_MODEL },
                    generationConfig =
                        generationConfig {
                            responseMimeType = "application/json"
                            thinkingConfig = thinkingConfig { thinkingLevel = ThinkingLevel.MINIMAL }
                        },
                    systemInstruction = content { text(AskPrompt.system(catalogSchema)) },
                )
            return model.generateContent(AskPrompt.user(question, schedule)).text.orEmpty()
        }
    }

private const val DEFAULT_MODEL = "gemini-3.5-flash"