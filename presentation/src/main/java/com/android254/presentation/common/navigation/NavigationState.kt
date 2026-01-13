/*
 * Copyright 2025 DroidconKE
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

/**
 * Remembers and creates a [NavigationState] to manage multi-stack navigation.
 *
 * This function initializes and persists the current top-level route and maintains
 * separate back stacks for each provided top-level destination.
 *
 * @param startRoute The initial route to be displayed when the app starts.
 * @param topLevelRoutes A set of all destination keys that represent top-level navigation items.
 * @return A [NavigationState] instance that tracks the current route and back stacks.
 */
@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>,
): NavigationState {
    val topLevelRoute =
        rememberSerializable(
            startRoute,
            topLevelRoutes,
            serializer = MutableStateSerializer(NavKeySerializer()),
        ) {
            mutableStateOf(startRoute)
        }

    val backStacks: Map<NavKey, NavBackStack<NavKey>> = topLevelRoutes.associateWith { key -> rememberNavBackStack(key) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
        )
    }
}

/**
 * Represents the state of navigation within the application, managing multiple back stacks
 * typically associated with top-level destinations (e.g., Bottom Navigation tabs).
 *
 * @property startRoute The initial root destination of the application.
 * @property topLevelRoute The currently active top-level destination.
 * @property backStacks A map associating each top-level route with its respective [NavBackStack].
 * @property stacksInUse A list of routes currently in the navigation hierarchy, used to determine
 * which stacks should be active or preserved.
 */
class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    var topLevelRoute: NavKey by topLevelRoute
    val stacksInUse: List<NavKey>
        get() =
            if (topLevelRoute == startRoute) {
                listOf(startRoute)
            } else {
                listOf(startRoute, topLevelRoute)
            }
}

@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries =
        backStacks.mapValues { (_, stack) ->
            val decorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                )
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = decorators,
                entryProvider = entryProvider,
            )
        }

    return stacksInUse
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList()
}