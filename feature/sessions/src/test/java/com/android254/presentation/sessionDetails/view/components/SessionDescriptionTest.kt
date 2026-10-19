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
package com.android254.presentation.sessionDetails.view.components

import com.android254.presentation.sessionDetails.view.components.DescriptionBlock.Item
import com.android254.presentation.sessionDetails.view.components.DescriptionBlock.Paragraph
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionDescriptionTest {
    @Test
    fun `indented continuation lines join, and lists keep their items`() {
        val raw =
            "Adding an A/B testing SDK takes an afternoon. Shipping a decision takes two skills\r\n" +
                "  most teams only half-learn.\r\n" +
                "\r\n" +
                " This session walks the lifecycle.\r\n" +
                "Running it:\r\n" +
                "  - Designing a test that can actually answer your question\r\n" +
                "  - Reading results with confidence\r\n" +
                "\r\n" +
                "  Engineering it:\r\n" +
                "  - One abstraction  over Statsig\r\n" +
                "\r\n" +
                "Key learning points\r\n" +
                "\r\n" +
                "1. Design a trustworthy experiment, with a\r\n" +
                "  pre-committed duration.\r\n" +
                "2. Log exposure honestly"

        assertEquals(
            listOf(
                Paragraph("Adding an A/B testing SDK takes an afternoon. Shipping a decision takes two skills most teams only half-learn."),
                Paragraph("This session walks the lifecycle."),
                Paragraph("Running it:"),
                Item("•", "Designing a test that can actually answer your question"),
                Item("•", "Reading results with confidence"),
                Paragraph("Engineering it:"),
                Item("•", "One abstraction over Statsig"),
                Paragraph("Key learning points"),
                Item("1.", "Design a trustworthy experiment, with a pre-committed duration."),
                Item("2.", "Log exposure honestly"),
            ),
            parseDescription(raw),
        )
    }

    @Test
    fun `an unindented line is a line of its own`() {
        val raw = "Experience level with the topic:\r\nSeven years in mobile security.\r\nWhat you get:\r\n- one\r\n- two"

        assertEquals(
            listOf(
                Paragraph("Experience level with the topic:"),
                Paragraph("Seven years in mobile security."),
                Paragraph("What you get:"),
                Item("•", "one"),
                Item("•", "two"),
            ),
            parseDescription(raw),
        )
    }

    @Test
    fun `a blank description has no blocks`() {
        assertEquals(emptyList<DescriptionBlock>(), parseDescription("  \r\n "))
    }
}