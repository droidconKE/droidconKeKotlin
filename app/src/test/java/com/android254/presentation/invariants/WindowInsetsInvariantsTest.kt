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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WindowInsetsInvariantsTest {
    @Test
    fun `every Scaffold declares the insets it consumes`() {
        val offenders =
            productionKotlinSources()
                .flatMap { file ->
                    file
                        .readText()
                        .argumentListsOf(SCAFFOLD_CALL)
                        .filterNot { it.contains("contentWindowInsets") }
                        .map { file.relativeTo(repoRoot).path }
                }.distinct()
                .sorted()
                .toList()

        assertEquals(
            "An edge-to-edge app has no sensible default here: a Scaffold either states the " +
                "insets it consumes or silently double-pads, or drops, someone else's. " +
                "Offending files:",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `no production source paints the system bars`() {
        val offenders =
            productionKotlinSources()
                .filter { file ->
                    file.readText().let {
                        it.contains("statusBarColor") || it.contains("navigationBarColor")
                    }
                }.map { it.relativeTo(repoRoot).path }
                .sorted()
                .toList()

        assertEquals(
            "Both are no-ops from API 35. Icon appearance comes from enableEdgeToEdge(), and " +
                "the bar itself is tinted by drawing under it. Offending files:",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `status bar icon appearance is overridden per screen, never shared`() {
        val offenders =
            productionKotlinSources()
                .filter { it.path.contains("/core/") }
                .filter { file -> STATUS_BAR_ICON_CALL.containsMatchIn(file.readText()) }
                .map { it.relativeTo(repoRoot).path }
                .sorted()
                .toList()

        assertEquals(
            "StatusBarIconAppearance belongs to the one screen that paints behind the status " +
                "bar, not to a theme or a shared component. Called from :core: it becomes the " +
                "global ChaiTheme SideEffect that §3.4 deleted. Offending files:",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `a screen that pads for the system bars and scrolls consumes what it padded`() {
        val offenders =
            productionKotlinSources()
                .filter { file ->
                    val text = file.readText()
                    PADS_BY_SCAFFOLD_INSETS.containsMatchIn(text) &&
                        SCROLLABLE.containsMatchIn(text) &&
                        !text.contains("consumeWindowInsets(")
                }.map { it.relativeTo(repoRoot).path }
                .sorted()
                .toList()

        assertEquals(
            "Padding a scrolling container by the insets and leaving them unconsumed pays for " +
                "them twice: once here, once in whatever inside reads safeDrawing again. The " +
                "pattern is padding(innerPadding).consumeWindowInsets(innerPadding). " +
                "Offending files:",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `a screen's lazy list takes the insets as contentPadding`() {
        val offenders =
            productionKotlinSources()
                .flatMap { file ->
                    val text = file.readText()
                    if (!text.handlesScreenInsets()) {
                        emptySequence()
                    } else {
                        text
                            .argumentListsOf(LAZY_SCROLLABLE)
                            .filterNot { it.contains("contentPadding") }
                            .map { file.relativeTo(repoRoot).path }
                            .asSequence()
                    }
                }.distinct()
                .sorted()
                .toList()

        assertEquals(
            "Insets belong in a lazy list's contentPadding, never as padding on its parent: " +
                "padding the parent clips the list and stops its content scrolling behind the " +
                "system bars. Offending files:",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `nothing applies safe-drawing padding to an adaptive scaffold`() {
        val offenders =
            productionKotlinSources()
                .filter { file -> file.readText().contains("safeDrawingPadding(") }
                .map { it.relativeTo(repoRoot).path }
                .sorted()
                .toList()

        assertEquals(
            "NavigationSuiteScaffold manages the insets its own navigation component covers and " +
                "hands the rest to the screens. Padding it clips the whole app out of the " +
                "system bar area and quietly ends edge-to-edge. Offending files:",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `the activity turns off navigation bar contrast enforcement`() {
        val activity = File(repoRoot, MAIN_ACTIVITY_PATH)

        assertTrue(
            "From SDK 29 the system paints its own translucent scrim behind three-button " +
                "navigation unless this is off, so the bottom bar's colour stops short of the " +
                "bottom of the screen. Expected `isNavigationBarContrastEnforced = false` in " +
                MAIN_ACTIVITY_PATH,
            activity.readText().contains("isNavigationBarContrastEnforced = false"),
        )
    }

    /**
     * Whether this file is a screen — something handed a `Scaffold`'s padding, or declaring the
     * inset contract, or taking a list's `contentPadding`. A component nested inside one of
     * those has no business reading insets and is not held to these rules.
     */
    private fun String.handlesScreenInsets(): Boolean =
        contains("paddingValues") ||
            contains("DroidconWindowInsets") ||
            contains("contentPadding: PaddingValues")

    /** The argument list of each call matching [call], trailing content lambda excluded. */
    private fun String.argumentListsOf(call: Regex): List<String> =
        call
            .findAll(this)
            .mapNotNull { match ->
                val open = match.range.last
                var depth = 0
                val close =
                    (open until length).firstOrNull { i ->
                        when (this[i]) {
                            '(' -> {
                                depth++
                                false
                            }

                            ')' -> {
                                depth--
                                depth == 0
                            }

                            else -> false
                        }
                    }
                close?.let { substring(open, it) }
            }.toList()

    private fun productionKotlinSources(): Sequence<File> =
        repoRoot
            .walkTopDown()
            .onEnter { it.name != "build" }
            .filter { it.isFile && it.extension == "kt" && it.path.contains("/src/main/") }

    private companion object {
        // Gradle runs unit tests with the module directory as the working directory.
        val repoRoot: File = File("..").canonicalFile

        // `Scaffold(`, but not `ListDetailPaneScaffold(` or any other suffix match.
        val SCAFFOLD_CALL = Regex("""(?<![A-Za-z0-9_])Scaffold\(""")

        // A call, not the declaration in :core:ui and not its import.
        val STATUS_BAR_ICON_CALL = Regex("""(?<!fun )(?<!import )\bStatusBarIconAppearance\(""")

        // Applying a Scaffold's own padding, as opposed to being handed a list's contentPadding.
        val PADS_BY_SCAFFOLD_INSETS = Regex("""\.padding\((paddingValues =\s*)?paddingValues\)""")

        val LAZY_SCROLLABLE =
            Regex("""(?<![A-Za-z0-9_])Lazy(Column|Row|VerticalGrid|HorizontalGrid|VerticalStaggeredGrid)\(""")

        val SCROLLABLE = Regex("""verticalScroll\(|horizontalScroll\(|""" + LAZY_SCROLLABLE.pattern)

        const val MAIN_ACTIVITY_PATH =
            "app/src/main/java/com/android254/presentation/activity/MainActivity.kt"
    }
}