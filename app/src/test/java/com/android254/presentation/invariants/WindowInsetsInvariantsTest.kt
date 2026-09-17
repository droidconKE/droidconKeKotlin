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

class WindowInsetsInvariantsTest {
    @Test
    fun `every Scaffold declares the insets it consumes`() {
        val offenders =
            productionKotlinSources()
                .flatMap { file ->
                    file
                        .readText()
                        .scaffoldArgumentLists()
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

    /**
     * The argument list of each `Scaffold(` call. The trailing content lambda falls outside the
     * parentheses, so a nested `Scaffold` is never mistaken for an argument of the outer one.
     */
    private fun String.scaffoldArgumentLists(): List<String> =
        SCAFFOLD_CALL
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
    }
}