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
package com.android254.presentation.invariants

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * The rules that keep the adaptive layer honest.
 *
 * Each one is something the Material adaptive guidance states outright and that is easy to undo
 * by reaching for the API that looks obvious in autocomplete.
 */
class AdaptiveInvariantsTest {
    @Test
    fun `multi-pane layouts are Navigation 3 scenes, never a pane scaffold`() {
        val offenders =
            productionKotlinSources()
                .filter { file -> PANE_SCAFFOLD.containsMatchIn(file.readText().withoutComments()) }
                .map { it.relativeTo(repoRoot).path }
                .sorted()
                .toList()

        assertEquals(
            "The adaptive guidance is explicit: use the Navigation 3 SceneStrategy approach, " +
                "not ListDetailPaneScaffold or SupportingPaneScaffold. Their navigators carry " +
                "a second back stack, which in this app would compete with " +
                "NavigationController.goBack(). Offending files:",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `no screen builds its own navigation bar`() {
        val offenders =
            productionKotlinSources()
                .filter { file -> OWN_NAVIGATION_BAR.containsMatchIn(file.readText().withoutComments()) }
                .map { it.relativeTo(repoRoot).path }
                .sorted()
                .toList()

        assertEquals(
            "The navigation area is NavigationSuiteScaffold's, so that it is a bar, a rail or a " +
                "drawer according to the window rather than a bar at the bottom of a screen " +
                "nobody's thumb can reach. Offending files:",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `layout decisions read the window, never the configuration`() {
        val offenders =
            productionKotlinSources()
                .filter { file -> CONFIGURATION_LAYOUT_READ.containsMatchIn(file.readText().withoutComments()) }
                .map { it.relativeTo(repoRoot).path }
                .sorted()
                .toList()

        assertEquals(
            "Configuration.screenWidthDp and LocalConfiguration.orientation are wrong in " +
                "multi-window, wrong on a foldable mid-fold and wrong in a resizable ChromeOS " +
                "window. rememberDroidconWindowSize() reads the window itself. Offending files:",
            emptyList<String>(),
            offenders,
        )
    }

    /**
     * The code, without the comments.
     *
     * These rules are about what the app calls, and every one of them is also something the
     * KDoc here explains you must not call — matching the prose would make the documentation
     * the violation.
     */
    private fun String.withoutComments(): String = replace(BLOCK_COMMENT, "").replace(LINE_COMMENT, "")

    private fun productionKotlinSources(): Sequence<File> =
        repoRoot
            .walkTopDown()
            .onEnter { it.name != "build" }
            .filter { it.isFile && it.extension == "kt" && it.path.contains("/src/main/") }

    private companion object {
        // Gradle runs unit tests with the module directory as the working directory.
        val repoRoot: File = File("..").canonicalFile

        // Every pane scaffold and every pane navigator. Written as (…Pane)Scaffold rather than
        // (…)PaneScaffold because the latter expands to "SupportingPanePaneScaffold", a type
        // that does not exist — which left the half of this rule the message talks about most
        // unable to fire at all.
        val PANE_SCAFFOLD =
            Regex(
                """\b(Navigable)?(ListDetailPane|SupportingPane|ThreePane)Scaffold\b""" +
                    """|\bremember(ListDetailPane|SupportingPane|ThreePane)ScaffoldNavigator\b""",
            )

        // The navigation components themselves, prefixed or not — `ShortNavigationBar` and
        // `WideNavigationRail` are the Expressive spellings of the two this rule exists to ban,
        // and a leading word boundary would let both straight through. The `Item` and `State`
        // suffixes are not matched, because those are legitimate inside a NavigationSuiteItem.
        val OWN_NAVIGATION_BAR =
            Regex(
                """(?<![A-Za-z0-9_])[A-Za-z]*""" +
                    """(BottomAppBar|BottomNavigation|NavigationBar|NavigationRail|NavigationDrawer|DrawerSheet)\(""",
            )

        // Any read of the configuration for layout, including the two-step idiom
        // `val configuration = LocalConfiguration.current` … `configuration.orientation`, which
        // matching the inline chain alone would miss. Nothing in src/main reads it for any
        // other reason, so the whole composition local is the rule.
        val CONFIGURATION_LAYOUT_READ =
            Regex("""\bLocalConfiguration\b|\b(screenWidthDp|screenHeightDp|smallestScreenWidthDp)\b""")

        val BLOCK_COMMENT = Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL)
        val LINE_COMMENT = Regex("""//[^\n]*""")
    }
}