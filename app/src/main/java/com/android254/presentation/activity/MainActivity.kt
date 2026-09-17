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
package com.android254.presentation.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.auth.AuthViewModel
import com.android254.presentation.auth.view.AuthDialog
import com.android254.presentation.common.adaptive.rememberDroidconWindowSize
import com.android254.presentation.common.adaptive.rememberIsMultiPaneWindow
import com.android254.presentation.common.adaptive.rememberShowsNavigationDrawer
import com.android254.presentation.common.livesessions.LiveSessionsRail
import com.android254.presentation.common.livesessions.rememberLiveSessions
import com.android254.presentation.common.navigation.DroidconDrawerHeader
import com.android254.presentation.common.navigation.DroidconNavigationItems
import com.android254.presentation.common.navigation.Navigation
import com.android254.presentation.common.navigation.NavigationController
import com.android254.presentation.common.navigation.Screens
import com.android254.presentation.common.navigation.bottomNavigationSet
import com.android254.presentation.common.navigation.droidconEntryProvider
import com.android254.presentation.common.navigation.droidconNavigationSuiteColors
import com.android254.presentation.common.navigation.navigationSuiteTypeFor
import com.android254.presentation.common.navigation.rememberNavigationState
import com.android254.presentation.common.navigation.shouldShowNavigation
import com.android254.presentation.common.navigation.shouldShowSupportingPane
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        // enableEdgeToEdge() turns contrast enforcement on, which paints the system's own
        // translucent scrim behind three-button navigation. The navigation bar and rail are
        // ours and already draw under it, so the scrim is a second, differently coloured bar
        // on top of the one we drew.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { viewModel.isInitialising.value }

        setContent {
            ChaiTheme {
                MainScreen(viewModel = viewModel)
            }
        }

        // Should move to the first bookmark: a launch-time prompt with no context gets
        // denied, and the rationale branch below only logs.
        askNotificationPermission()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Timber.i("Permission Granted")
            } else {
                Timber.i("Permission Denied")
            }
        }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Timber.i("Permission Granted")
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                Timber.i("Should Show Rationale")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val navigationState =
        rememberNavigationState(
            startRoute = Screens.Home,
            topLevelRoutes = bottomNavigationSet,
        )
    val navController = remember { NavigationController(navigationState) }
    val sessionsState by viewModel.sessionState.collectAsStateWithLifecycle()
    var showAuthDialog by remember {
        mutableStateOf(false)
    }

    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            viewModel = { authViewModel },
        )
    }

    val activity = LocalActivity.current
    val windowSize = rememberDroidconWindowSize()
    val isMultiPaneWindow = rememberIsMultiPaneWindow()
    val navigationSuiteType = navigationSuiteTypeFor(windowSize, rememberShowsNavigationDrawer())

    val currentRoute = navigationState.currentRoute
    val showNavigation = shouldShowNavigation(currentRoute, isMultiPaneWindow)

    // The navigation area animates in and out rather than being added and removed, so the
    // content does not jump a bar's height on the way to a detail.
    val navigationSuiteState = rememberNavigationSuiteScaffoldState()
    LaunchedEffect(showNavigation) {
        if (showNavigation) navigationSuiteState.show() else navigationSuiteState.hide()
    }

    val liveSessions = rememberLiveSessions(sessionsState)

    // Two presentations of the same sessions: a supporting pane where there is a column to
    // spare, the horizontal rail everywhere else.
    val showSupportingPane =
        shouldShowSupportingPane(
            route = currentRoute,
            isMultiPaneWindow = isMultiPaneWindow,
            hasLiveSessions = liveSessions.isNotEmpty(),
        )
    val showLiveSessionsRail =
        showNavigation && !isMultiPaneWindow && liveSessions.isNotEmpty()

    NavigationSuiteScaffold(
        modifier =
            modifier
                .fillMaxSize()
                .semantics { testTagsAsResourceId = true },
        navigationItems = {
            DroidconNavigationItems(
                currentTopLevelRoute = navigationState.topLevelRoute,
                navigationSuiteType = navigationSuiteType,
                onNavigate = navController::navigate,
            )
        },
        navigationSuiteType = navigationSuiteType,
        navigationSuiteColors = droidconNavigationSuiteColors(),
        containerColor = MaterialTheme.chaiColorsPalette.background,
        state = navigationSuiteState,
        primaryActionContent = {
            if (navigationSuiteType == NavigationSuiteType.NavigationDrawer) {
                DroidconDrawerHeader()
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Navigation(
                // `NavDisplay` decides whether to intercept back from the entries it is given,
                // and at expanded widths those include the standing supporting pane — so it
                // intercepts on the start destination too, where there is nothing to pop.
                // Without this, back on a tablet's landing screen would do nothing at all.
                onBack = { if (!navController.goBack()) activity?.finish() },
                modifier =
                    Modifier
                        .weight(1f)
                        .then(
                            // The rail below is the bottom-most thing on screen and pays for
                            // the bottom inset, so the screens above must not pay it again.
                            if (showLiveSessionsRail) {
                                Modifier.consumeWindowInsets(
                                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                                )
                            } else {
                                Modifier
                            },
                        ),
                navController = navController,
                navigationState = navigationState,
                supportingRoute = Screens.HappeningNow.takeIf { showSupportingPane },
                entryProvider =
                    droidconEntryProvider(
                        navController = navController,
                        onActionClicked = { showAuthDialog = !showAuthDialog },
                    ),
            )
            if (showLiveSessionsRail) {
                LiveSessionsRail(
                    sessions = liveSessions,
                    onSessionClick = { navController.navigate(Screens.SessionDetails(it)) },
                )
            }
        }
    }
}