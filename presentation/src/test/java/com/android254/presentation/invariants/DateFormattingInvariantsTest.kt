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
 * B9 and B16 removed every `SimpleDateFormat` from production code. It is not thread-safe, and
 * both sites that had it were reachable from more than one thread.
 *
 * A code change alone does not keep it out — this does.
 */
class DateFormattingInvariantsTest {
    @Test
    fun `no production source uses SimpleDateFormat`() {
        val offenders =
            repoRoot
                .walkTopDown()
                .filter { it.isProductionKotlinSource() }
                .filter { it.readText().contains("SimpleDateFormat") }
                .map { it.relativeTo(repoRoot).path }
                .sorted()
                .toList()

        assertEquals(
            "SimpleDateFormat is not thread-safe; use kotlinx-datetime. Offending files:",
            emptyList<String>(),
            offenders,
        )
    }

    private fun File.isProductionKotlinSource(): Boolean =
        isFile &&
            extension == "kt" &&
            path.contains("/src/main/") &&
            !path.contains("/build/")

    private companion object {
        /** Gradle runs unit tests with the module directory as the working directory. */
        val repoRoot: File = File("..").canonicalFile
    }
}