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
package com.android254.presentation.sessionDetails.view.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyLarge
import ke.droidcon.kotlin.presentation.R

@Composable
fun TopBar(onNavigationIconClick: () -> Unit) {
    TopAppBar(
        modifier = Modifier.testTag(TestTag.TOP_BAR),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.chaiColorsPalette.background,
                navigationIconContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                scrolledContainerColor = MaterialTheme.chaiColorsPalette.background,
                titleContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                actionIconContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
            ),
        title = {
            ChaiBodyLarge(
                bodyText = stringResource(id = R.string.session_details_label),
                textColor = MaterialTheme.chaiColorsPalette.textBoldColor,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { onNavigationIconClick() },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = stringResource(R.string.back_arrow_icon_description),
                    tint = MaterialTheme.chaiColorsPalette.textBoldColor,
                )
            }
        },
    )
}