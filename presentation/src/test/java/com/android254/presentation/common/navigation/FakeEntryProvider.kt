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
package com.android254.presentation.common.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider

inline fun <reified T : NavKey> EntryProviderScope<NavKey>.fakeEntry() {
    entry<T> { key ->
        FakePlaceholderScreen(screen = key as Screens)
    }
}

@Composable
fun fakeEntryProvider(): (NavKey) -> NavEntry<NavKey> {
    val provider =
        entryProvider<NavKey> {
            fakeEntry<Screens.Home>()
            fakeEntry<Screens.Sessions>()
            fakeEntry<Screens.SessionDetails>()
            fakeEntry<Screens.Feed>()
            fakeEntry<Screens.About>()
            fakeEntry<Screens.Speakers>()
            fakeEntry<Screens.FeedBack>()
            fakeEntry<Screens.SpeakerDetails>()
        }
    return provider
}

@Composable
fun FakePlaceholderScreen(screen: Screens) {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Display the base title (from the Screens class property)
        Text(
            text = "Screen: ${screen.title}",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Dynamically extract arguments based on the specific subclass
        val argumentText =
            when (screen) {
                is Screens.SessionDetails -> "Argument (Session ID): ${screen.sessionId}"
                is Screens.SpeakerDetails -> "Argument (Speaker Name): ${screen.speakerName}"
                // Objects don't have arguments, so we can return a default string
                else -> "No extra arguments for this screen"
            }

        Text(
            text = argumentText,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}