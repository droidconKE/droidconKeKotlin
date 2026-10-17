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

class StringResourceInvariantsTest {
    @Test
    fun `no string name is defined in more than one module`() {
        val files = defaultStringFiles().toList()
        assertTrue("Found no strings.xml under $repoRoot; the scan is looking in the wrong place", files.size > 1)

        val owners: Map<String, List<String>> =
            files
                .flatMap { file -> STRING_NAME.findAll(file.readText()).map { it.groupValues[2] to file.module() } }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, modules) -> modules.distinct().sorted() }
        val duplicated = owners.filterValues { it.size > 1 }.toSortedMap()

        assertEquals(
            "A name defined twice resolves to whichever module wins the merge; the copies drift. Duplicates:",
            emptyMap<String, List<String>>(),
            duplicated,
        )
    }

    private fun defaultStringFiles(): Sequence<File> =
        repoRoot
            .walkTopDown()
            .onEnter { it.name != "build" && !it.name.startsWith(".") }
            .filter { it.isFile && it.name == "strings.xml" && it.path.endsWith("/src/main/res/values/strings.xml") }

    /** `core/ui/src/main/res/values/strings.xml` → `core/ui`. */
    private fun File.module(): String = relativeTo(repoRoot).path.substringBefore("/src/main/")

    private companion object {
        // Gradle runs unit tests with the module directory as the working directory.
        val repoRoot: File = File("..").canonicalFile

        val STRING_NAME = Regex("""<(string|plurals|string-array)\s+name="([^"]+)"""")
    }
}