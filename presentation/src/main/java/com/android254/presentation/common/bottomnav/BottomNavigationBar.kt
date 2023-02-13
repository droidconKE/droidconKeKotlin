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

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android254.presentation.common.navigation.Screens
import com.android254.presentation.common.navigation.bottomNavigationDestinations
import com.android254.presentation.common.theme.DroidconKE2022Theme
import com.android254.presentation.common.theme.bottomBlack
import com.android254.presentation.common.theme.bottomOrange
import com.android254.presentation.common.theme.bottomPurple
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomAppBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavigationDestinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                icon = {
                    Icon(
                        painter = painterResource(id = destination.icon),
                        contentDescription = destination.title
                    )
                },
                label = { Text(text = destination.title) },
                alwaysShowLabel = true,
                onClick = {
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Screens.Home.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = bottomPurple,
                    unselectedIconColor = bottomBlack,
                    selectedTextColor = bottomOrange,
                    unselectedTextColor = bottomBlack

                )
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    DroidconKE2022Theme {
        BottomNavigationBar(rememberNavController())
    }
}