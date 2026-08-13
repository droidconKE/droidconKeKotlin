/*
 * Copyright 2026 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android254

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

/**
 * Android Lint, configured the same way in every module.
 *
 * There is no baseline file. A baseline freezes today's violations and then quietly grows,
 * and the one this project used to carry had drifted so far that most of its entries no
 * longer described anything real. Severity decisions live in `config/lint/lint.xml`
 * instead, where each one is a reviewed line in a diff.
 */
internal fun Project.configureLint(commonExtension: CommonExtension) {
    // `lint` is a property on CommonExtension, not an Action-taking function — the block
    // form only exists in the Kotlin DSL accessors, which precompiled plugins do not see.
    commonExtension.lint.apply {
        // Deliberately not `warningsAsErrors = true`. Severity is decided per rule in
        // config/lint/lint.xml: the rules this project enforces are promoted to `error`
        // there, and the ones still being burned down stay at `warning` so they show up in
        // reports and in the IDE without holding the build hostage.
        abortOnError = true

        // Tests are shipped code too — a leaked context in a test still leaks.
        checkTestSources = true

        // Generated sources belong to whoever generated them.
        checkGeneratedSources = false

        // One reviewed place for every severity decision, shared by all modules.
        lintConfig = rootProject.file("config/lint/lint.xml")

        // AGP 9 always writes the HTML, XML, SARIF and text reports, so the *Report toggles
        // are gone. CI uploads them straight out of build/reports/.
    }
}
