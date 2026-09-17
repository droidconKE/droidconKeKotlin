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

import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.android254.presentation.common.livesessions.HappeningNowPaneWidth
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Distinguishes the two list-detail scaffolds sharing one `NavDisplay`. */
enum class DroidconPaneScene {
    Sessions,
    Speakers,
}

/** List-detail first: with a session open, the right-hand column belongs to it. */
@Composable
fun rememberDroidconSceneStrategies(): ImmutableList<SceneStrategy<NavKey>> {
    // One entry per back press: the Material default would pop past the list and out of the tab,
    // because it assumes the list is a sibling entry rather than a tab root.
    val listDetail =
        rememberListDetailSceneStrategy<NavKey>(
            backNavigationBehavior = BackNavigationBehavior.PopUntilCurrentDestinationChange,
        )
    val supporting = rememberSupportingPaneSceneStrategy<NavKey>()
    return remember(listDetail, supporting) {
        persistentListOf(
            ListPaneRequiredSceneStrategy(listDetail),
            SupportingPaneRequiredSceneStrategy(supporting),
        )
    }
}

/**
 * Declines the list-detail scene when the list it would draw is not on the back stack.
 *
 * The Material strategy expands a second pane whenever there is room and fills it with whatever
 * list entry it can find — nothing, for a session opened from Home. Declining hands the entries
 * on, so the detail takes the window with its back arrow intact.
 */
internal class ListPaneRequiredSceneStrategy<T : Any>(
    private val delegate: SceneStrategy<T>,
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val openDetailScene = entries.lastOrNull()?.metadata?.get(DETAIL_SCENE_KEY)
        if (openDetailScene != null &&
            entries.none { it.metadata[LIST_SCENE_KEY] == openDetailScene }
        ) {
            return null
        }
        return with(delegate) { calculateScene(entries) }
    }
}

// The library's pane metadata is internal and NavEntry does not expose its key.
internal const val LIST_SCENE_KEY = "ke.droidcon.kotlin.listPaneScene"
internal const val DETAIL_SCENE_KEY = "ke.droidcon.kotlin.detailPaneScene"
internal const val SUPPORTING_SCENE_KEY = "ke.droidcon.kotlin.supportingPaneScene"

/** Metadata marking an entry as the list half of [scene]. */
fun listPaneMetadata(
    scene: DroidconPaneScene,
    detailPlaceholder: @Composable () -> Unit,
): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = scene,
        detailPlaceholder = { detailPlaceholder() },
    ) + SupportingPaneSceneStrategy.mainPane() + mapOf(LIST_SCENE_KEY to scene)

/** Metadata marking an entry as the detail half of [scene]. */
fun detailPaneMetadata(scene: DroidconPaneScene): Map<String, Any> = ListDetailSceneStrategy.detailPane(sceneKey = scene) + mapOf(DETAIL_SCENE_KEY to scene)

/** A destination that is never a detail still needs a role, or the supporting pane finds nothing. */
fun mainPaneMetadata(): Map<String, Any> = SupportingPaneSceneStrategy.mainPane()

/** Metadata for the standing "happening now" pane. */
fun supportingPaneMetadata(): Map<String, Any> =
    SupportingPaneSceneStrategy.supportingPane() +
        SupportingPaneSceneStrategy.preferredPaneSize(width = HappeningNowPaneWidth) +
        mapOf(SUPPORTING_SCENE_KEY to true)

/**
 * Declines the supporting-pane scene when there is no supporting entry to put in it.
 *
 * Same trap as the list-detail one: every top-level destination carries a main-pane role, so
 * from 840 dp the Material strategy forms a two-pane scene out of the main entry alone and
 * squeezes the screen into a fraction of the window beside an empty column.
 */
internal class SupportingPaneRequiredSceneStrategy<T : Any>(
    private val delegate: SceneStrategy<T>,
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (entries.none { it.metadata.containsKey(SUPPORTING_SCENE_KEY) }) return null
        return with(delegate) { calculateScene(entries) }
    }
}