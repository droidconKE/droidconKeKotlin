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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationItemColors
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.android254.presentation.common.adaptive.DroidconWindowSize
import com.android254.presentation.common.adaptive.clickablePointer
import com.droidconke.chai.isDarkTheme
import ke.droidcon.kotlin.core.ui.R

/** [showsDrawer] matches the app bars, so the logo cannot end up in neither. A phone on its side keeps the bottom bar. */
fun navigationSuiteTypeFor(
    windowSize: DroidconWindowSize,
    showsDrawer: Boolean,
    phoneOnItsSide: Boolean,
): NavigationSuiteType =
    when {
        windowSize == DroidconWindowSize.Compact || phoneOnItsSide -> NavigationSuiteType.ShortNavigationBarCompact
        showsDrawer -> NavigationSuiteType.WideNavigationRailExpanded
        else -> NavigationSuiteType.WideNavigationRailCollapsed
    }

@Composable
fun droidconNavigationSuiteColors(): NavigationSuiteColors {
    val barContainer = MaterialTheme.colorScheme.surfaceContainerLowest
    val railContainer = MaterialTheme.colorScheme.surfaceContainer
    return NavigationSuiteDefaults.colors(
        shortNavigationBarContainerColor = barContainer,
        wideNavigationRailColors = WideNavigationRailDefaults.colors(containerColor = railContainer),
        navigationBarContainerColor = barContainer,
        navigationRailContainerColor = railContainer,
        navigationDrawerContainerColor = railContainer,
    )
}

internal fun selectedNavigationLabelColor(
    navigationSuiteType: NavigationSuiteType,
    colorScheme: ColorScheme,
): Color =
    when (navigationSuiteType) {
        NavigationSuiteType.WideNavigationRailExpanded,
        NavigationSuiteType.NavigationDrawer,
        -> colorScheme.onSecondaryContainer

        else -> colorScheme.secondary
    }

@Composable
fun DroidconNavigationItems(
    currentTopLevelRoute: NavKey,
    navigationSuiteType: NavigationSuiteType,
    onNavigate: (Screens) -> Unit,
) {
    val selectedLabelColor = selectedNavigationLabelColor(navigationSuiteType, MaterialTheme.colorScheme)
    val labelStyle =
        if (navigationSuiteType == NavigationSuiteType.WideNavigationRailExpanded) {
            MaterialTheme.typography.labelLarge
        } else {
            MaterialTheme.typography.labelSmall
        }
    TopLevelDestination.entries.forEach { destination ->
        val isSelected = destination.route == currentTopLevelRoute
        NavigationSuiteItem(
            navigationSuiteType = navigationSuiteType,
            selected = isSelected,
            onClick = { onNavigate(destination.route) },
            modifier = Modifier.testTag("nav_${destination.name.lowercase()}").clickablePointer(),
            icon = {
                Icon(
                    painter = painterResource(id = destination.icon),
                    // Decorative: the label carries the name.
                    contentDescription = null,
                )
            },
            label = {
                Text(
                    text = stringResource(destination.label),
                    style = labelStyle,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) selectedLabelColor else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            },
            colors =
                NavigationItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColorTopIconPosition = MaterialTheme.colorScheme.secondary,
                    selectedTextColorStartIconPosition = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledIconColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        )
    }
}

@Composable
fun DroidconDrawerHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 16.dp),
    ) {
        Image(
            painter =
                painterResource(
                    id =
                        if (MaterialTheme.isDarkTheme) {
                            R.drawable.droidcon_logo_dark
                        } else {
                            R.drawable.droidcon_logo
                        },
                ),
            contentDescription = stringResource(id = R.string.logo),
        )
    }
}