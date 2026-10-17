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
import android.content.Intent
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.auth.AuthViewModel
import com.android254.presentation.auth.view.AuthDialog
import com.android254.presentation.common.adaptive.rememberDroidconWindowSize
import com.android254.presentation.common.adaptive.rememberIsMultiPaneWindow
import com.android254.presentation.common.adaptive.rememberIsPhoneOnItsSide
import com.android254.presentation.common.adaptive.rememberShowsNavigationDrawer
import com.android254.presentation.common.livesessions.LiveSessionsRail
import com.android254.presentation.common.livesessions.rememberLiveSessions
import com.android254.presentation.common.navigation.DeepLinkEffect
import com.android254.presentation.common.navigation.DroidconDrawerHeader
import com.android254.presentation.common.navigation.DroidconNavigationItems
import com.android254.presentation.common.navigation.Navigation
import com.android254.presentation.common.navigation.NavigationController
import com.android254.presentation.common.navigation.Screens
import com.android254.presentation.common.navigation.bottomNavigationSet
import com.android254.presentation.common.navigation.deepLinkFrom
import com.android254.presentation.common.navigation.droidconEntryProvider
import com.android254.presentation.common.navigation.droidconNavigationSuiteColors
import com.android254.presentation.common.navigation.navigationSuiteTypeFor
import com.android254.presentation.common.navigation.rememberNavigationState
import com.android254.presentation.common.navigation.shouldShowLiveSessionsRail
import com.android254.presentation.common.navigation.shouldShowNavigation
import com.android254.presentation.common.navigation.shouldShowSupportingPane
import com.droidconke.chai.ChaiTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    /** A notification's destination, waiting for the navigation to open it. */
    private var pendingDeepLink by mutableStateOf<Screens?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        // Contrast enforcement would paint a second bar over ours under three-button navigation.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { viewModel.isInitialising.value }

        // A restored activity already navigated to its link before it was destroyed.
        if (savedInstanceState == null) pendingDeepLink = deepLinkFrom(intent)

        setContent {
            ChaiTheme {
                MainScreen(
                    viewModel = viewModel,
                    deepLink = pendingDeepLink,
                    onDeepLinkHandled = { pendingDeepLink = null },
                )
                NotificationPermissionPrompt(
                    requests = viewModel.notificationPermissionRequests,
                    onPrompted = viewModel::onNotificationPermissionPrompted,
                    launch = ::launchNotificationPermissionRequest,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkFrom(intent)?.let { pendingDeepLink = it }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            Timber.i("Notification permission %s", if (isGranted) "granted" else "denied")
        }

    private fun launchNotificationPermissionRequest() {
        // The view model only asks where the permission is a runtime one.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

/**
 * Opens the system notification prompt for each of [requests].
 *
 * [onPrompted] runs before [launch], so the prompt counts as asked even if the process dies
 * while the dialog is up.
 */
@Composable
fun NotificationPermissionPrompt(
    requests: Flow<Unit>,
    onPrompted: () -> Unit,
    launch: () -> Unit,
) {
    val currentOnPrompted by rememberUpdatedState(onPrompted)
    val currentLaunch by rememberUpdatedState(launch)
    LaunchedEffect(requests) {
        requests.collect {
            currentOnPrompted()
            currentLaunch()
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    deepLink: Screens? = null,
    onDeepLinkHandled: () -> Unit = {},
) {
    val navigationState =
        rememberNavigationState(
            startRoute = Screens.Home,
            topLevelRoutes = bottomNavigationSet,
        )
    val navController = remember { NavigationController(navigationState) }
    DeepLinkEffect(deepLink = deepLink, navController = navController, onHandled = onDeepLinkHandled)
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
    val navigationSuiteType = navigationSuiteTypeFor(windowSize, rememberShowsNavigationDrawer(), rememberIsPhoneOnItsSide())

    val currentRoute = navigationState.currentRoute
    val showNavigation = shouldShowNavigation(currentRoute, isMultiPaneWindow)

    // Animated so content does not jump on the way to a detail.
    val navigationSuiteState = rememberNavigationSuiteScaffoldState()
    LaunchedEffect(showNavigation) {
        if (showNavigation) navigationSuiteState.show() else navigationSuiteState.hide()
    }

    val liveSessions = rememberLiveSessions(sessionsState)

    // A pane where there is a column, a rail elsewhere.
    val showSupportingPane =
        shouldShowSupportingPane(
            route = currentRoute,
            isMultiPaneWindow = isMultiPaneWindow,
            hasLiveSessions = liveSessions.isNotEmpty(),
        )
    val showLiveSessionsRail =
        shouldShowLiveSessionsRail(
            route = currentRoute,
            isMultiPaneWindow = isMultiPaneWindow,
            liveSessionCount = liveSessions.size,
            liveNowCount = sessionsState.current.size,
        )

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
        containerColor = MaterialTheme.colorScheme.background,
        state = navigationSuiteState,
        primaryActionContent = {
            if (navigationSuiteType == NavigationSuiteType.WideNavigationRailExpanded) {
                DroidconDrawerHeader()
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Navigation(
                // With the supporting pane listed, NavDisplay intercepts back even at the start destination.
                onBack = { if (!navController.goBack()) activity?.finish() },
                modifier =
                    Modifier
                        .weight(1f)
                        .then(
                            // The rail below pays the bottom inset, so the screens must not.
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