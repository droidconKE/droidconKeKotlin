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
package com.android254.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.home.viewstate.HomeHero
import com.android254.presentation.models.SessionPresentationModel
import com.droidconke.chai.ChaiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Native graphics, so text measures with the real font rather than one pixel per character.
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class HomeHeaderSectionComponentTest {
    @get:Rule
    val rule = createComposeRule()

    private var large = TextUnit.Unspecified
    private var medium = TextUnit.Unspecified

    @Test
    fun `the welcome title keeps the hero role beside the cube`() {
        show()

        assertEquals(large, fontSizeOf("Welcome to droidconKenya"))
    }

    @Test
    fun `a live title that would run past three lines steps down a role`() {
        val session = fakeSessions.first().copy(title = LONG_TITLE)
        show(liveSession = session)

        assertEquals(medium, fontSizeOf(session.title))
    }

    @Test
    fun `a short live title keeps the hero role`() {
        val session = fakeSessions.minBy { it.title.length }
        assertTrue(session.title.length < SHORT_TITLE)
        show(liveSession = session)

        assertEquals(large, fontSizeOf(session.title))
    }

    private fun show(liveSession: SessionPresentationModel? = null) {
        rule.setContent {
            ChaiTheme {
                large = MaterialTheme.typography.titleLargeEmphasized.fontSize
                medium = MaterialTheme.typography.titleMediumEmphasized.fontSize
                Box(Modifier.width(PHONE_WIDTH)) {
                    HomeHeaderSectionComponent(hero = HomeHero.Welcome, liveSession = liveSession, showCube = true, animateCube = false)
                }
            }
        }
    }

    private fun fontSizeOf(text: String): TextUnit {
        val results = mutableListOf<TextLayoutResult>()
        rule
            .onNodeWithText(text, useUnmergedTree = true)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action!!(results)
        return results
            .single()
            .layoutInput.style.fontSize
    }

    private companion object {
        val PHONE_WIDTH = 360.dp
        const val LONG_TITLE = "Architecture at Scale: From MVVM to Event-Driven Patterns - A Production Journey"
        const val SHORT_TITLE = 30
    }
}