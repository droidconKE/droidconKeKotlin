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
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.auth.AuthViewModel
import com.android254.presentation.auth.view.AuthDialog
import com.android254.presentation.common.bottomnav.BottomNavigationBar
import com.android254.presentation.common.navigation.Navigation
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Timber.i("Notification permission is :$isGranted")
    }

    @Inject
    lateinit var remoteFeatureToggle: RemoteFeatureToggle

    @Inject
    lateinit var syncDataWorkManager: SyncDataWorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var keepSplashScreen = true
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            keepSplashScreen
        }
        checkForNotificationPermission()
        lifecycleScope.launch {
            if (remoteFeatureToggle.syncNowIfEmpty()) {
                syncDataWorkManager.startSync()
            }
            keepSplashScreen = false
        }
        setContent {
            ChaiDCKE22Theme {
                MainScreen()
            }
        }
    }

    private fun checkForNotificationPermission() {
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
fun MainScreen() {
    val authViewModel = hiltViewModel<AuthViewModel>()
    val navController = rememberNavController()
    val bottomBarState = rememberSaveable { (mutableStateOf(true)) }
    var showAuthDialog by remember {
        mutableStateOf(false)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { if (bottomBarState.value) BottomNavigationBar(navController) },
        containerColor = MaterialTheme.chaiColorsPalette.background
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            if (showAuthDialog) {
                AuthDialog(
                    onDismiss = { showAuthDialog = false },
                    viewModel = { authViewModel }
                )
            }
            Navigation(
                navController = navController,
                updateBottomBarState = { bottomBarState.value = it },
                onActionClicked = {
                    showAuthDialog = !showAuthDialog
                }
            )
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen()
}