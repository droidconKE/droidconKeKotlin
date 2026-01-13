package com.android254.presentation.common.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
fun fakeEntryProvider() : (NavKey) -> NavEntry<NavKey> {

    val provider = entryProvider<NavKey> {
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Display the base title (from the Screens class property)
        Text(
            text = "Screen: ${screen.title}",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Dynamically extract arguments based on the specific subclass
        val argumentText = when (screen) {
            is Screens.SessionDetails -> "Argument (Session ID): ${screen.sessionId}"
            is Screens.SpeakerDetails -> "Argument (Speaker Name): ${screen.speakerName}"
            // Objects don't have arguments, so we can return a default string
            else -> "No extra arguments for this screen"
        }

        Text(
            text = argumentText,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}