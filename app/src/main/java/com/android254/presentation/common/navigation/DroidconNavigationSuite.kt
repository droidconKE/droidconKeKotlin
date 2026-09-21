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

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.android254.presentation.common.adaptive.DroidconWindowSize
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiTextLabelSmall
import ke.droidcon.kotlin.core.ui.R

/** [showsDrawer] is the same value the app bars use, so the logo cannot end up in neither. */
fun navigationSuiteTypeFor(
    windowSize: DroidconWindowSize,
    showsDrawer: Boolean,
): NavigationSuiteType =
    when {
        windowSize == DroidconWindowSize.Compact -> NavigationSuiteType.NavigationBar
        showsDrawer -> NavigationSuiteType.NavigationDrawer
        else -> NavigationSuiteType.NavigationRail
    }

/** The chai palette, for whichever navigation component the window ends up with. */
@Composable
fun droidconNavigationSuiteColors(): NavigationSuiteColors {
    val container = MaterialTheme.chaiColorsPalette.bottomNavBackgroundColor
    return NavigationSuiteDefaults.colors(
        navigationBarContainerColor = container,
        shortNavigationBarContainerColor = container,
        navigationRailContainerColor = container,
        navigationDrawerContainerColor = container,
    )
}

/** The type decides whether an item draws as a bar item, a rail item or a drawer row. */
@Composable
fun DroidconNavigationItems(
    currentTopLevelRoute: NavKey,
    navigationSuiteType: NavigationSuiteType,
    onNavigate: (Screens) -> Unit,
) {
    TopLevelDestination.entries.forEach { destination ->
        val isSelected = destination.route == currentTopLevelRoute
        NavigationSuiteItem(
            navigationSuiteType = navigationSuiteType,
            selected = isSelected,
            onClick = { onNavigate(destination.route) },
            modifier = Modifier.testTag("nav_${destination.name.lowercase()}"),
            icon = {
                Icon(
                    painter = painterResource(id = destination.icon),
                    // Decorative: the label beside it carries the name for screen readers.
                    contentDescription = null,
                )
            },
            label = {
                ChaiTextLabelSmall(
                    bodyText = stringResource(destination.label),
                    textColor =
                        if (isSelected) {
                            MaterialTheme.chaiColorsPalette.activeBottomNavTextColor
                        } else {
                            MaterialTheme.chaiColorsPalette.textNormalColor
                        },
                )
            },
            colors =
                NavigationItemColors(
                    selectedIconColor = MaterialTheme.chaiColorsPalette.activeBottomNavIconColor,
                    selectedTextColor = MaterialTheme.chaiColorsPalette.activeBottomNavTextColor,
                    selectedIndicatorColor =
                        MaterialTheme.chaiColorsPalette.activeBottomNavIconColor
                            .copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.chaiColorsPalette.inactiveBottomNavIconColor,
                    unselectedTextColor = MaterialTheme.chaiColorsPalette.textNormalColor,
                    disabledIconColor = MaterialTheme.chaiColorsPalette.inactiveBottomNavIconColor,
                    disabledTextColor = MaterialTheme.chaiColorsPalette.textWeakColor,
                ),
        )
    }
}

/** The drawer's header, in the primary-action slot, which only the drawer draws. */
@Composable
fun DroidconDrawerHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 16.dp),
    ) {
        Image(
            painter =
                painterResource(
                    id =
                        if (isSystemInDarkTheme()) {
                            R.drawable.droidcon_logo_dark
                        } else {
                            R.drawable.droidcon_logo
                        },
                ),
            contentDescription = stringResource(id = R.string.logo),
        )
    }
}