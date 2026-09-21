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
package com.android254.presentation.common.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Both Material strategies will build a two-pane scene out of one entry, starving the pane that
 * has content. This asserts the rule the guards encode, not the width the library then measures.
 */
class PaneGuardTest {
    @Test
    fun `list-detail is declined when the detail has no list behind it`() {
        val delegated =
            listDetailGuardRuns(
                entry(mainPaneMetadata()),
                entry(detailPaneMetadata(DroidconPaneScene.Sessions)),
            )

        assertFalse("A session opened from Home has no list to sit beside", delegated)
    }

    @Test
    fun `list-detail is allowed when the list is on the stack`() {
        val delegated =
            listDetailGuardRuns(
                entry(mainPaneMetadata()),
                entry(listPaneMetadata(DroidconPaneScene.Sessions) {}),
                entry(detailPaneMetadata(DroidconPaneScene.Sessions)),
            )

        assertTrue(delegated)
    }

    @Test
    fun `list-detail is allowed for a list on its own, which draws the placeholder`() {
        val delegated =
            listDetailGuardRuns(
                entry(mainPaneMetadata()),
                entry(listPaneMetadata(DroidconPaneScene.Sessions) {}),
            )

        assertTrue(delegated)
    }

    @Test
    fun `a detail is matched to its own list, not to any list`() {
        val delegated =
            listDetailGuardRuns(
                entry(mainPaneMetadata()),
                entry(listPaneMetadata(DroidconPaneScene.Speakers) {}),
                entry(detailPaneMetadata(DroidconPaneScene.Sessions)),
            )

        assertFalse("A speakers list is not something a session can sit beside", delegated)
    }

    @Test
    fun `supporting is declined when nothing carries a supporting role`() {
        val delegated = supportingGuardRuns(entry(mainPaneMetadata()))

        assertFalse(
            "Every top-level destination carries a main-pane role, so without this the " +
                "strategy builds a two-pane scene out of the main entry alone",
            delegated,
        )
    }

    @Test
    fun `supporting is allowed once there is something to support with`() {
        val delegated =
            supportingGuardRuns(
                entry(mainPaneMetadata()),
                entry(supportingPaneMetadata()),
            )

        assertTrue(delegated)
    }

    private fun listDetailGuardRuns(vararg entries: NavEntry<NavKey>): Boolean = guardRuns(entries.toList()) { ListPaneRequiredSceneStrategy(it) }

    private fun supportingGuardRuns(vararg entries: NavEntry<NavKey>): Boolean = guardRuns(entries.toList()) { SupportingPaneRequiredSceneStrategy(it) }

    private fun guardRuns(
        entries: List<NavEntry<NavKey>>,
        wrap: (SceneStrategy<NavKey>) -> SceneStrategy<NavKey>,
    ): Boolean {
        var reached = false
        val guard =
            wrap(
                SceneStrategy<NavKey> {
                    reached = true
                    null
                },
            )
        with(SceneStrategyScope<NavKey>()) { with(guard) { calculateScene(entries) } }
        return reached
    }

    private fun entry(metadata: Map<String, Any>): NavEntry<NavKey> = NavEntry(key = Screens.Home, metadata = metadata) { }
}