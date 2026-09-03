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
package com.droidconke.chai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.droidconke.chai.components.COutlinedPrimaryButton
import com.droidconke.chai.components.CPrimaryButton
import com.droidconke.chai.components.ChaiBodyLarge
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiBodySmall
import com.droidconke.chai.components.ChaiBodySmallBold
import com.droidconke.chai.components.ChaiBodyXSmall
import com.droidconke.chai.components.ChaiBodyXSmallBold
import com.droidconke.chai.components.ChaiSubTitle
import com.droidconke.chai.components.ChaiTextButtonLight
import com.droidconke.chai.components.ChaiTextLabelLarge
import com.droidconke.chai.components.ChaiTextLabelMedium
import com.droidconke.chai.components.ChaiTextLabelSmall
import com.droidconke.chai.components.ChaiTitle
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import org.junit.Test

/**
 * Goldens for chai's component surface.
 *
 * The text scale is captured as one image on purpose: hardcoded `sp` line heights clip at
 * 200% font scale, and that only reads as a defect when the whole ramp is seen together.
 */
class ChaiComponentScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun `primary button enabled`() =
        captureComponent("chai/primary_button_enabled") {
            Padded {
                CPrimaryButton(
                    onClick = {},
                    isEnabled = true,
                    title = "Filter",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

    @Test
    fun `primary button disabled`() =
        captureComponent("chai/primary_button_disabled") {
            Padded {
                CPrimaryButton(
                    onClick = {},
                    isEnabled = false,
                    title = "Filter",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

    @Test
    fun `outlined primary button`() =
        captureComponent("chai/outlined_primary_button") {
            Padded {
                COutlinedPrimaryButton(
                    onClick = {},
                    title = "Twitter",
                    icon = Icons.Outlined.Save,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

    @Test
    fun `text scale`() =
        captureComponent("chai/text_scale") {
            Padded {
                ChaiTitle(titleText = "ChaiTitle")
                ChaiSubTitle(titleText = "ChaiSubTitle")
                ChaiBodyLargeBold(bodyText = "ChaiBodyLargeBold")
                ChaiBodyLarge(bodyText = "ChaiBodyLarge")
                ChaiBodyMediumBold(bodyText = "ChaiBodyMediumBold")
                ChaiBodyMedium(bodyText = "ChaiBodyMedium")
                ChaiBodySmallBold(bodyText = "ChaiBodySmallBold")
                ChaiBodySmall(bodyText = "ChaiBodySmall")
                ChaiBodyXSmallBold(bodyText = "ChaiBodyXSmallBold")
                ChaiBodyXSmall(bodyText = "ChaiBodyXSmall")
                ChaiTextButtonLight(bodyText = "ChaiTextButtonLight")
                ChaiTextLabelLarge(bodyText = "ChaiTextLabelLarge")
                ChaiTextLabelMedium(bodyText = "ChaiTextLabelMedium")
                ChaiTextLabelSmall(bodyText = "ChaiTextLabelSmall")
            }
        }

    /** Multi-line copy is where a too-small line height actually collides. */
    @Test
    fun `body text wrapping`() =
        captureComponent("chai/body_text_wrapping") {
            Padded {
                ChaiBodyMedium(bodyText = WRAPPING_COPY)
                ChaiBodySmall(bodyText = WRAPPING_COPY)
                ChaiBodyXSmall(bodyText = WRAPPING_COPY)
            }
        }

    private companion object {
        const val WRAPPING_COPY =
            "Sessions run in Sapphire and Opal, and a talk can be scheduled in both rooms " +
                "at once, which is exactly the case that wraps onto a third line."
    }
}

@Composable
private fun Padded(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}