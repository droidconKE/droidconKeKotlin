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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

class NavKeyInvariantsTest {
    private val resourceAnnotations =
        setOf(
            DrawableRes::class.java,
            StringRes::class.java,
            IdRes::class.java,
            ColorRes::class.java,
        )

    private fun destinationClasses(root: Class<*> = Screens::class.java): List<Class<*>> =
        root.declaredClasses
            .filter { Screens::class.java.isAssignableFrom(it) }
            .flatMap { nested ->
                if (nested.isInterface || Modifier.isAbstract(nested.modifiers)) {
                    destinationClasses(nested)
                } else {
                    listOf(nested)
                }
            }

    @Test
    fun `every navigation key is serializable, immutable, and holds no resource ids`() {
        val destinations = destinationClasses()
        assertTrue("Expected at least one Screens destination", destinations.isNotEmpty())

        val violations = mutableListOf<String>()
        for (destination in destinations) {
            val name = destination.simpleName
            val hasSerializer =
                destination.annotations.any { it.annotationClass == Serializable::class } ||
                    destination.declaredClasses.any { it.simpleName == "Companion" } ||
                    destination.declaredMethods.any { it.name == "serializer" }
            if (!hasSerializer) {
                violations += "$name is missing @Serializable"
            }
            for (field in destination.declaredFields) {
                if (Modifier.isStatic(field.modifiers) || field.isSynthetic) continue
                if (!Modifier.isFinal(field.modifiers)) {
                    violations += "$name.${field.name} is mutable (var)"
                }
                val hasResAnnotation = field.annotations.any { it.annotationClass.java in resourceAnnotations }
                if (hasResAnnotation || field.type == Int::class.javaPrimitiveType || field.type == Int::class.javaObjectType) {
                    violations += "$name.${field.name} holds an Int/resource ID; keep display metadata in TopLevelDestination"
                }
            }
        }

        assertTrue(
            "Navigation key invariant violations:\n${violations.joinToString("\n")}",
            violations.isEmpty(),
        )
    }
}