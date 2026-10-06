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
package com.droidconke.chai.atoms

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChaiTypographyTest {
    private val emphasizedByBase: Map<String, Pair<TextStyle, TextStyle>> =
        with(ChaiTypography) {
            mapOf(
                "displayLarge" to (displayLarge to displayLargeEmphasized),
                "displayMedium" to (displayMedium to displayMediumEmphasized),
                "displaySmall" to (displaySmall to displaySmallEmphasized),
                "headlineLarge" to (headlineLarge to headlineLargeEmphasized),
                "headlineMedium" to (headlineMedium to headlineMediumEmphasized),
                "headlineSmall" to (headlineSmall to headlineSmallEmphasized),
                "titleLarge" to (titleLarge to titleLargeEmphasized),
                "titleMedium" to (titleMedium to titleMediumEmphasized),
                "titleSmall" to (titleSmall to titleSmallEmphasized),
                "bodyLarge" to (bodyLarge to bodyLargeEmphasized),
                "bodyMedium" to (bodyMedium to bodyMediumEmphasized),
                "bodySmall" to (bodySmall to bodySmallEmphasized),
                "labelLarge" to (labelLarge to labelLargeEmphasized),
                "labelMedium" to (labelMedium to labelMediumEmphasized),
                "labelSmall" to (labelSmall to labelSmallEmphasized),
            )
        }

    @Test
    fun `every emphasized role keeps its base role's font, size and line height`() {
        emphasizedByBase.forEach { (role, styles) ->
            val (base, emphasized) = styles
            assertNotNull("$role font", base.fontFamily)
            assertEquals("$role font", base.fontFamily, emphasized.fontFamily)
            assertEquals("$role size", base.fontSize, emphasized.fontSize)
            assertEquals("$role line height", base.lineHeight, emphasized.lineHeight)
        }
    }

    @Test
    fun `every emphasized role is one bundled weight heavier than its base role`() {
        emphasizedByBase.forEach { (role, styles) ->
            val baseWeight = requireNotNull(styles.first.fontWeight)
            val emphasizedWeight = requireNotNull(styles.second.fontWeight)
            assertTrue("$role weight $emphasizedWeight vs $baseWeight", emphasizedWeight > baseWeight)
            assertTrue("$role weight $emphasizedWeight", emphasizedWeight <= FontWeight.ExtraBold)
        }
    }
}