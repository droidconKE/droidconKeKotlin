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
package ke.droidcon.kotlin.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import com.droidconke.chai.ChaiTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Base for chai screenshot tests.
 *
 * Captures every subject in light, dark, and at 200% font scale. Font scale is the axis that
 * catches text clipped by hardcoded `sp` line heights, so it is not optional.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
abstract class ChaiScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** Tight bounds around a single component. */
    protected fun captureComponent(
        name: String,
        content: @Composable () -> Unit,
    ) = capture(name = name, fillWindow = false, content = content)

    /** Full-window capture, for whole screens. */
    protected fun captureScreen(
        name: String,
        content: @Composable () -> Unit,
    ) = capture(name = name, fillWindow = true, content = content)

    private fun capture(
        name: String,
        fillWindow: Boolean,
        content: @Composable () -> Unit,
    ) {
        var variant by mutableStateOf(ScreenshotVariant.entries.first())

        // setContent may only be called once per test, so the matrix is driven by state and
        // re-rendered between captures rather than by setting the content repeatedly.
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, variant.fontScale),
            ) {
                ChaiTheme(darkTheme = variant.isDark) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        val sizing =
                            if (fillWindow) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier.fillMaxWidth().wrapContentHeight()
                            }
                        Box(modifier = sizing.testTag(CAPTURE_TAG)) { content() }
                    }
                }
            }
        }

        ScreenshotVariant.entries.forEach { entry ->
            variant = entry
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(CAPTURE_TAG).captureRoboImage(
                filePath = "src/test/screenshots/$name/${entry.id}.png",
                roborazziOptions =
                    RoborazziOptions(
                        compareOptions =
                            RoborazziOptions.CompareOptions(
                                // Absorbs font-rendering noise across JDKs without hiding
                                // real layout changes.
                                changeThreshold = CHANGE_THRESHOLD,
                            ),
                    ),
            )
        }
    }

    enum class ScreenshotVariant(
        val id: String,
        val isDark: Boolean,
        val fontScale: Float,
    ) {
        Light("light", false, 1f),
        Dark("dark", true, 1f),
        LightFont200("light_font_200", false, 2f),
    }

    private companion object {
        const val CAPTURE_TAG = "chai_screenshot_capture_root"
        const val CHANGE_THRESHOLD = 0.001f
    }
}