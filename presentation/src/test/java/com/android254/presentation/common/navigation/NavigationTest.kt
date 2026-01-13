package com.android254.presentation.common.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavEntry
import com.droidconke.chai.ChaiDCKE22Theme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import kotlin.intArrayOf

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun `should show start route`(){
        val startScreen = Screens.Home
        val navigationState = mutableStateOf<NavigationState?>(null)
        composeTestRule.setContent {
            ChaiDCKE22Theme {
                navigationState.value =
                    rememberNavigationState(
                        startRoute = startScreen,
                        topLevelRoutes = bottomNavigationDestinations,
                    )
                val navController = remember { navigationState.value?.let { NavigationController(it) } }

                navigationState.value?.let {
                    Navigation(
                        navController = navController!!,
                        navigationState = it,
                        updateBottomBarState = {},
                        entryProvider = fakeEntryProvider()
                    )
                }
            }
        }

        val stacksInUse = navigationState.value?.stacksInUse

        composeTestRule.onNodeWithTag("navigation_display").assertIsDisplayed()
        composeTestRule.onNodeWithText("Screen: ${startScreen.title}").assertIsDisplayed()
        assert(stacksInUse?.size == 1)
    }

    @Test
    fun `navigate to top level should update UI and state`(){
        val startScreen = Screens.Home
        val destinationScreen = Screens.About
        val navigationState = mutableStateOf<NavigationState?>(null)

        composeTestRule.setContent {
            ChaiDCKE22Theme {
                navigationState.value =
                    rememberNavigationState(
                        startRoute = startScreen,
                        topLevelRoutes = bottomNavigationDestinations,
                    )
                val navController = remember { navigationState.value?.let { NavigationController(it) } }

                navigationState.value?.let {
                    Column(
                        Modifier.fillMaxSize()
                    ){
                        Navigation(
                            navController = navController!!,
                            navigationState = it,
                            updateBottomBarState = {},
                            entryProvider = fakeEntryProvider()
                        )

                        Button(onClick = {
                            navController.navigate(destinationScreen)
                        }) {
                            Text(text = "Navigate to ${destinationScreen.title}")
                        }
                    }
                }

            }
        }

        composeTestRule.onNodeWithTag("navigation_display").assertExists()
        composeTestRule.onNodeWithText("Screen: ${startScreen.title}").assertIsDisplayed()

        composeTestRule.onNodeWithText("Navigate to ${destinationScreen.title}").assertIsDisplayed()

        composeTestRule.onNodeWithText("Navigate to ${destinationScreen.title}").performClick()

        composeTestRule.onNodeWithText("Screen: ${destinationScreen.title}").assertIsDisplayed()
    }

    @Test
    fun `navigate to screen details passes arguments correctly`() {
        val startScreen = Screens.Home
        val navigationState = mutableStateOf<NavigationState?>(null)
        val sessionId = "123"
        val destinationScreen = Screens.SessionDetails(sessionId)

        composeTestRule.setContent {
            ChaiDCKE22Theme {
                navigationState.value =
                    rememberNavigationState(
                        startRoute = startScreen,
                        topLevelRoutes = bottomNavigationDestinations,
                    )
                val navController = remember { navigationState.value?.let { NavigationController(it) } }

                navigationState.value?.let {
                    Column(
                        Modifier.fillMaxSize()
                    ){
                        Navigation(
                            navController = navController!!,
                            navigationState = it,
                            updateBottomBarState = {},
                            entryProvider = fakeEntryProvider()
                        )

                        Button(onClick = {
                            navController.navigate(destinationScreen)
                        }) {
                            Text(text = "Navigate to ${destinationScreen.title}")
                        }
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("navigation_display").assertIsDisplayed()
        composeTestRule.onNodeWithText("Screen: ${startScreen.title}").assertIsDisplayed()

        composeTestRule.onNodeWithText("Navigate to ${destinationScreen.title}").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigate to ${destinationScreen.title}").performClick()

        composeTestRule.onNodeWithText("Screen: ${destinationScreen.title}").assertIsDisplayed()
        composeTestRule.onNodeWithText("Argument (Session ID): ${destinationScreen.sessionId}").assertIsDisplayed()
    }


}