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

/**
 * The scene strategies, in the order `NavDisplay` should try them.
 *
 * List-detail comes first: with a session open beside its list, the right-hand column belongs to
 * the session, not to what else is on right now.
 */
@Composable
fun rememberDroidconSceneStrategies(): ImmutableList<SceneStrategy<NavKey>> {
    val listDetail = rememberListDetailSceneStrategy<NavKey>()
    val supporting = rememberSupportingPaneSceneStrategy<NavKey>()
    return remember(listDetail, supporting) {
        persistentListOf(ListPaneRequiredSceneStrategy(listDetail), supporting)
    }
}

/**
 * Declines the list-detail scene when the list it would draw is not on the back stack.
 *
 * `ListDetailSceneStrategy` expands a second pane whenever the window has room for one, and
 * fills the list pane with whatever list entry it can find — nothing, if the detail was opened
 * from somewhere else. Tapping a session on Home pushes only the detail, and the result on a
 * tablet is a real detail beside an empty column.
 *
 * Declining hands the entries to the next strategy, which is how the detail ends up full-width
 * with the navigation drawer still beside it. The alternative — pushing the list first so the
 * pane has something to show — would change what back does on a phone, where tapping a session
 * on Home and pressing back has always returned to Home.
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

// Our own markers rather than the library's, whose pane metadata is internal, and rather than
// the entry key, which NavEntry does not expose.
internal const val LIST_SCENE_KEY = "ke.droidcon.kotlin.listPaneScene"
internal const val DETAIL_SCENE_KEY = "ke.droidcon.kotlin.detailPaneScene"

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

/**
 * Metadata for a destination that is never a detail.
 *
 * It still needs a role: without one, the walk back from the "happening now" entry stops at it
 * and the supporting pane draws beside nothing.
 */
fun mainPaneMetadata(): Map<String, Any> = SupportingPaneSceneStrategy.mainPane()

/** Metadata for the standing "happening now" pane. */
fun supportingPaneMetadata(): Map<String, Any> =
    SupportingPaneSceneStrategy.supportingPane() +
        SupportingPaneSceneStrategy.preferredPaneSize(width = HappeningNowPaneWidth)