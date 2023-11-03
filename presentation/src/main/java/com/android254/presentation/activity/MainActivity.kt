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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import dagger.hilt.android.AndroidEntryPoint
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
        bottomBar = { if (bottomBarState.value) BottomNavigationBar(navController) }
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