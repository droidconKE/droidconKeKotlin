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
package com.android254.presentation.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavigationController,
    navigationState: NavigationState,
    updateBottomBarState: (Boolean) -> Unit,
    onActionClicked: () -> Unit = {},
    entryProvider: (NavKey) -> NavEntry<NavKey> =
        droidconEntryProvider(
            updateBottomBarState,
            navController,
            onActionClicked,
        ),
) {
    NavDisplay(
        modifier =
            modifier
                .testTag("navigation_display"),
        entries = navigationState.toEntries(entryProvider),
        onBack = { navController.goBack() },
    )
}