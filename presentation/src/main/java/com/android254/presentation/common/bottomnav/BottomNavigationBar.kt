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
package com.android254.presentation.common.bottomnav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.common.navigation.NavigationController
import com.android254.presentation.common.navigation.NavigationState
import com.android254.presentation.common.navigation.Screens
import com.android254.presentation.common.navigation.bottomNavigationSet
import com.android254.presentation.common.navigation.rememberNavigationState
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionStatus
import com.android254.presentation.sessions.components.CurrentSessionComponent
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiTextLabelSmall

@Composable
fun BottomNavigationBar(
    navController: NavigationController,
    navigationState: NavigationState,
    currentSessions: List<SessionPresentationModel> = emptyList(),
    upNextSessions: List<SessionPresentationModel> = emptyList(),
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(currentSessions, key = { "current-${it.id}" }) { session ->
                CurrentSessionComponent(
                    modifier = Modifier.fillParentMaxWidth(0.85f),
                    session = session,
                ) { id ->
                    navController.navigate(Screens.SessionDetails(id))
                }
            }
            items(upNextSessions, key = { "next-${it.id}" }) { session ->
                CurrentSessionComponent(
                    modifier = Modifier.fillParentMaxWidth(0.85f),
                    session = session,
                ) { id ->
                    navController.navigate(Screens.SessionDetails(id))
                }
            }
        }
        BottomAppBar(
            modifier =
                Modifier
                    .background(MaterialTheme.chaiColorsPalette.bottomNavBorderColor)
                    .padding(top = 1.dp),
            containerColor = MaterialTheme.chaiColorsPalette.bottomNavBackgroundColor,
        ) {
            val topLevelRoute = navigationState.topLevelRoute

            bottomNavigationSet.forEach { destination ->
                val selected = destination == topLevelRoute

                BottomNavItem(
                    isSelected = selected,
                    destination = destination,
                    onClick = {
                        navController.navigate(destination)
                    },
                )
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    isSelected: Boolean,
    destination: Screens,
    onClick: () -> Unit,
) {
    val iconColor =
        if (isSelected) {
            MaterialTheme.chaiColorsPalette.activeBottomNavIconColor
        } else {
            MaterialTheme.chaiColorsPalette.inactiveBottomNavIconColor
        }

    val textColor =
        if (isSelected) {
            MaterialTheme.chaiColorsPalette.activeBottomNavTextColor
        } else {
            MaterialTheme.chaiColorsPalette.textNormalColor
        }

    Column(
        modifier =
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    onClick = onClick,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(id = destination.icon),
            contentDescription = destination.title,
            tint = iconColor,
        )
        Spacer(modifier = Modifier.height(6.dp))

        ChaiTextLabelSmall(
            modifier = Modifier,
            bodyText = destination.title,
            textColor = textColor,
        )
    }
}

@PreviewLightDark
@Composable
fun BottomNavigationBarPreview() {
    ChaiDCKE22Theme {
        val navigationState =
            rememberNavigationState(
                startRoute = Screens.Home,
                topLevelRoutes = bottomNavigationSet,
            )
        val navController = remember { NavigationController(navigationState) }

        Surface(
            color = MaterialTheme.chaiColorsPalette.background,
        ) {
            BottomNavigationBar(
                navController = navController,
                navigationState = navigationState,
                currentSessions = fakeSessions.filter { it.sessionStatus == SessionStatus.Ongoing }.take(2),
                upNextSessions = fakeSessions.filter { it.sessionStatus == SessionStatus.Upcoming }.take(2),
            )
        }
    }
}