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

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * The destination a launch [intent] asks for, or null.
 *
 * An activity reopened from recents is handed its original intent again; replaying the link
 * would push the destination a second time, so those launches carry none.
 */
fun deepLinkFrom(intent: Intent?): Screens? {
    if (intent?.action != Intent.ACTION_VIEW) return null
    if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return null
    return intent.dataString?.let(DeepLinks::parse)
}

/** Opens [deepLink] when there is one, then reports it handled so it is not opened again. */
@Composable
fun DeepLinkEffect(
    deepLink: Screens?,
    navController: NavigationController,
    onHandled: () -> Unit,
) {
    LaunchedEffect(deepLink) {
        if (deepLink != null) {
            navController.navigate(deepLink)
            onHandled()
        }
    }
}