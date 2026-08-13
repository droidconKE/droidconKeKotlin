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
 * There is no baseline. Severity is decided per rule in `config/lint/lint.xml`.
 */
internal fun Project.configureLint(commonExtension: CommonExtension) {
    commonExtension.lint.apply {
        abortOnError = true
        checkTestSources = true
        checkGeneratedSources = false
        lintConfig = rootProject.file("config/lint/lint.xml")
    }
}
