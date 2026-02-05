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

    @Test
    fun `goBack from nested screen should return to previous screen in stack`() {
        val startScreen = Screens.Home
        val destinationScreen = Screens.SessionDetails("123")
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

        val navController = NavigationController(navigationState.value!!)

        // Navigate to nested screen
        composeTestRule.runOnUiThread {
            navController.navigate(destinationScreen)
        }
        composeTestRule.onNodeWithText("Screen: ${destinationScreen.title}").assertIsDisplayed()

        // Go back
        composeTestRule.runOnUiThread {
            navController.goBack()
        }
        composeTestRule.onNodeWithText("Screen: ${startScreen.title}").assertIsDisplayed()
    }

    @Test
    fun `goBack from top-level route (not startRoute) should return to startRoute`() {
        val startScreen = Screens.Home
        val otherTopLevel = Screens.About
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

        val navController = NavigationController(navigationState.value!!)

        // Navigate to another top-level screen
        composeTestRule.runOnUiThread {
            navController.navigate(otherTopLevel)
        }
        composeTestRule.onNodeWithText("Screen: ${otherTopLevel.title}").assertIsDisplayed()

        // Go back
        composeTestRule.runOnUiThread {
            navController.goBack()
        }
        composeTestRule.onNodeWithText("Screen: ${startScreen.title}").assertIsDisplayed()
    }

    @Test
    fun `goBack from startRoute root should do nothing or maintain state`() {
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

        val navController = NavigationController(navigationState.value!!)

        // Go back from root
        composeTestRule.runOnUiThread {
            navController.goBack()
        }

        // Should still be on Home
        composeTestRule.onNodeWithText("Screen: ${startScreen.title}").assertIsDisplayed()
    }

    @Test
    fun `navigate to currently active top-level route should not change state`() {
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

        val navController = NavigationController(navigationState.value!!)
        val initialState = navigationState.value?.topLevelRoute

        // Navigate to Home again
        composeTestRule.runOnUiThread {
            navController.navigate(startScreen)
        }

        assert(navigationState.value?.topLevelRoute == initialState)
        composeTestRule.onNodeWithText("Screen: ${startScreen.title}").assertIsDisplayed()
    }

    @Test
    fun `deep nesting within a stack`() {
        val startScreen = Screens.Home
        val nested1 = Screens.SessionDetails("1")
        val nested2 = Screens.SpeakerDetails("Speaker A")
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

        val navController = NavigationController(navigationState.value!!)

        // Navigate deep
        composeTestRule.runOnUiThread {
            navController.navigate(nested1)
            navController.navigate(nested2)
        }

        composeTestRule.onNodeWithText("Screen: ${nested2.title}").assertIsDisplayed()
        composeTestRule.onNodeWithText("Argument (Speaker Name): ${nested2.speakerName}").assertIsDisplayed()

        // Go back once
        composeTestRule.runOnUiThread {
            navController.goBack()
        }
        composeTestRule.onNodeWithText("Screen: ${nested1.title}").assertIsDisplayed()

        // Go back twice
        composeTestRule.runOnUiThread {
            navController.goBack()
        }
        composeTestRule.onNodeWithText("Screen: ${startScreen.title}").assertIsDisplayed()
    }

    @Test
    fun `navigateUp should not switch top-level routes`() {
        val startScreen = Screens.Home
        val otherTopLevel = Screens.About
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

        val navController = NavigationController(navigationState.value!!)

        // Navigate to other top-level
        composeTestRule.runOnUiThread {
            navController.navigate(otherTopLevel)
        }
        composeTestRule.onNodeWithText("Screen: ${otherTopLevel.title}").assertIsDisplayed()

        // navigateUp from root of About should NOT go back to Home (unlike goBack)
        composeTestRule.runOnUiThread {
            navController.navigateUp()
        }
        composeTestRule.onNodeWithText("Screen: ${otherTopLevel.title}").assertIsDisplayed()
        assert(navigationState.value?.topLevelRoute == otherTopLevel)
    }

}