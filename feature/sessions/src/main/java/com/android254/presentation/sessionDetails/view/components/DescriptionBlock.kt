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

internal sealed interface DescriptionBlock {
    val text: String

    data class Paragraph(
        override val text: String,
    ) : DescriptionBlock

    data class Item(
        val marker: String,
        override val text: String,
    ) : DescriptionBlock
}

private val bullet = Regex("""^[-•*–]\s+(.+)""")
private val numbered = Regex("""^(\d{1,2})[.)]\s+(.+)""")
private val runOfSpaces = Regex("""[ \t]{2,}""")

/** Talk abstracts arrive hard-wrapped from the CFP form: a wrapped line is indented and follows text. */
internal fun parseDescription(raw: String): List<DescriptionBlock> {
    val blocks = mutableListOf<DescriptionBlock>()
    var afterText = false
    raw.replace("\r\n", "\n").replace('\r', '\n').split('\n').forEach { rawLine ->
        val line = rawLine.replace(runOfSpaces, " ").trim()
        val last = blocks.lastOrNull()
        val bulletMatch = bullet.find(line)
        val numberedMatch = numbered.find(line)
        val wrapped = afterText && rawLine.firstOrNull()?.isWhitespace() == true && last != null && !last.text.endsWith(':')
        when {
            line.isEmpty() -> Unit
            bulletMatch != null -> blocks += DescriptionBlock.Item("•", bulletMatch.groupValues[1])
            numberedMatch != null -> blocks += DescriptionBlock.Item("${numberedMatch.groupValues[1]}.", numberedMatch.groupValues[2])
            wrapped -> blocks[blocks.lastIndex] = last.join(line)
            else -> blocks += DescriptionBlock.Paragraph(line)
        }
        afterText = line.isNotEmpty()
    }
    return blocks
}

private fun DescriptionBlock.join(line: String): DescriptionBlock =
    when (this) {
        is DescriptionBlock.Paragraph -> copy(text = "$text $line")
        is DescriptionBlock.Item -> copy(text = "$text $line")
    }