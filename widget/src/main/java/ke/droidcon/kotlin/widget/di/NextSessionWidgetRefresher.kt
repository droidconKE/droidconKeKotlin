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
package ke.droidcon.kotlin.widget.di

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.widget.WidgetRefresher
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.droidcon.kotlin.widget.NextSessionWidget
import javax.inject.Inject

class NextSessionWidgetRefresher
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val sessionsRepo: SessionsRepo,
    ) : WidgetRefresher {
        override suspend fun refresh() {
            NextSessionWidget(sessionsRepo).updateAll(context)
        }
    }