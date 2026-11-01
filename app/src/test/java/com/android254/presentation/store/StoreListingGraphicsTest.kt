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
package com.android254.presentation.store

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.hero.CubeHeroBanner
import ke.droidcon.kotlin.R
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config
import java.io.File
import ke.droidcon.kotlin.core.ui.R as UiR

/** Draws the Play listing's icon and feature graphic. Only runs with STORE_GRAPHICS=1; see playstore/README.md. */
@Config(qualifiers = "w1100dp-h700dp-xhdpi")
class StoreListingGraphicsTest : ChaiScreenshotTest() {
    @Before
    fun onlyWhenAsked() = assumeTrue(System.getenv("STORE_GRAPHICS") == "1")

    /** The launcher icon's layers, cropped to the 72 dp a launcher shows; Play rounds the corners. */
    @Test
    fun icon() =
        render("icon.png", Bitmap.CompressFormat.PNG) {
            Box(
                modifier =
                    Modifier
                        .size(256.dp)
                        .background(colorResource(R.color.ic_launcher_background))
                        .clipToBounds(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(384.dp),
                )
            }
        }

    /** A JPEG, because Play refuses a feature graphic with an alpha channel. */
    @Test
    fun featureGraphic() =
        render("featureGraphic.jpg", Bitmap.CompressFormat.JPEG) {
            Box(
                modifier =
                    Modifier
                        .size(512.dp, 250.dp)
                        .background(MaterialTheme.chaiColorsPalette.heroContainerColor),
                contentAlignment = Alignment.Center,
            ) {
                CubeHeroBanner(animateCube = false) {
                    Image(
                        painter = painterResource(UiR.drawable.droidcon_logo_dark),
                        contentDescription = null,
                        modifier = Modifier.height(32.dp),
                    )
                    Text(
                        text = stringResource(UiR.string.about_banner_title),
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        color = MaterialTheme.chaiColorsPalette.heroContentColor,
                    )
                    Text(
                        text = stringResource(UiR.string.about_banner_subtitle),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.chaiColorsPalette.heroOnContainerColor,
                    )
                }
            }
        }

    private fun render(
        name: String,
        format: Bitmap.CompressFormat,
        content: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            ChaiTheme(darkTheme = false) { Box(Modifier.testTag(TAG)) { content() } }
        }
        composeRule.waitForIdle()
        val bitmap = composeRule.onNodeWithTag(TAG).captureToImage().asAndroidBitmap()
        File(LISTING_IMAGES, name).outputStream().use { bitmap.compress(format, QUALITY, it) }
    }

    private companion object {
        const val TAG = "store_graphic"
        const val QUALITY = 95
        val LISTING_IMAGES = File("../playstore/en-US/images")
    }
}