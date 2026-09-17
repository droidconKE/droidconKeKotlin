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

/** Rules the Material adaptive guidance states outright and autocomplete makes easy to undo. */
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

    /** The code without the comments: every rule here is also something the KDoc names. */
    private fun String.withoutComments(): String = replace(BLOCK_COMMENT, "").replace(LINE_COMMENT, "")

    private fun productionKotlinSources(): Sequence<File> =
        repoRoot
            .walkTopDown()
            .onEnter { it.name != "build" }
            .filter { it.isFile && it.extension == "kt" && it.path.contains("/src/main/") }

    private companion object {
        // Gradle runs unit tests with the module directory as the working directory.
        val repoRoot: File = File("..").canonicalFile

        // (…Pane)Scaffold, not (…)PaneScaffold: the latter expands to SupportingPanePaneScaffold.
        val PANE_SCAFFOLD =
            Regex(
                """\b(Navigable)?(ListDetailPane|SupportingPane|ThreePane)Scaffold\b""" +
                    """|\bremember(ListDetailPane|SupportingPane|ThreePane)ScaffoldNavigator\b""",
            )

        // Prefixed spellings too: ShortNavigationBar and WideNavigationRail are the Expressive
        // names for the two this bans. The capital keeps rememberShowsNavigationDrawer out.
        val OWN_NAVIGATION_BAR =
            Regex(
                """(?<![A-Za-z0-9_])([A-Z][A-Za-z]*)?""" +
                    """(BottomAppBar|BottomNavigation|NavigationBar|NavigationRail|NavigationDrawer|DrawerSheet)\(""",
            )

        // The whole composition local, since matching the inline chain misses the hoisted idiom.
        val CONFIGURATION_LAYOUT_READ =
            Regex("""\bLocalConfiguration\b|\b(screenWidthDp|screenHeightDp|smallestScreenWidthDp)\b""")

        val BLOCK_COMMENT = Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL)
        val LINE_COMMENT = Regex("""//[^\n]*""")
    }
}