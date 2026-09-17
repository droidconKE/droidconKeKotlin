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

/**
 * A controller responsible for managing app navigation and back stack state.
 *
 * This controller handles logic for switching between top-level routes and managing
 * nested navigation stacks within those routes.
 *
 * @property state The current navigation state being managed by this controller.
 */
class NavigationController(
    val state: NavigationState,
) {
    fun navigate(route: Screens) {
        val toIndex = bottomNavigationRoutes.indexOf(route)
        val isNested = toIndex == -1

        val lastDirection =
            if (isNested) {
                NavDirection.INNER
            } else {
                val fromIndex = bottomNavigationRoutes.indexOf(state.topLevelRoute)
                if (fromIndex != -1 && fromIndex != toIndex) {
                    if (toIndex > fromIndex) NavDirection.LEFT else NavDirection.RIGHT
                } else {
                    NavDirection.INNER
                }
            }

        state.lastDirection = lastDirection

        if (!isNested) {
            // This is a top level route, just switch to it.
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun navigateUp() {
        val currentStack = state.backStacks[state.topLevelRoute]
        // Only remove if we are not at the base of the stack
        if (currentStack != null && currentStack.last() != state.topLevelRoute) {
            state.lastDirection = NavDirection.INNER
            currentStack.removeLastOrNull()
        }
    }

    /**
     * Handles a back event.
     *
     * @return whether anything moved. False means the start destination was already showing with
     * nothing on its stack, so the press is the system's to deal with — the caller must let it
     * through rather than swallow it. `NavDisplay` decides whether to intercept back from the
     * entries it was given, and at expanded widths those include the standing supporting pane,
     * so it intercepts on the start destination too; without the fall-through, back on a
     * tablet's landing screen would do nothing at all.
     */
    fun goBack(): Boolean {
        val currentStack = state.backStacks[state.topLevelRoute] ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == state.topLevelRoute) {
            if (state.topLevelRoute == state.startRoute) return false
            state.lastDirection = NavDirection.RIGHT
            state.topLevelRoute = state.startRoute
            return true
        }
        state.lastDirection = NavDirection.INNER
        return currentStack.removeLastOrNull() != null
    }
}