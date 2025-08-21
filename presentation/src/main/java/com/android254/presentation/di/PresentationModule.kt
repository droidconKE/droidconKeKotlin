/*
 * Copyright 2023 DroidconKE
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
package com.android254.presentation.di


import com.android254.presentation.about.view.AboutViewModel
import com.android254.presentation.auth.AuthViewModel
import com.android254.presentation.feed.FeedViewModel
import com.android254.presentation.home.viewmodel.HomeViewModel
import com.android254.presentation.notifications.DroidconNotificationManager
import com.android254.presentation.sessionDetails.SessionDetailsViewModel
import com.android254.presentation.sessions.view.SessionsViewModel
import com.android254.presentation.speakers.SpeakerDetailsScreenViewModel
import com.android254.presentation.speakers.SpeakersScreenViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule: Module = module {
    single { DroidconNotificationManager(androidContext()) }
    viewModelOf(::AuthViewModel)
    viewModelOf(::AboutViewModel)
    viewModelOf(::FeedViewModel)
    viewModelOf(::SessionsViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(:: SpeakerDetailsScreenViewModel)
    viewModelOf(:: SpeakersScreenViewModel)
    viewModel {
        SessionDetailsViewModel(
            sessionsRepo = get(),
            savedStateHandle = get()
        )
    }
}