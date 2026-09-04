/*
 * Copyright 2022 DroidconKE
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
package com.droidconke.chai.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

// A Color.Unspecified default lets the text inherit from whatever drew it, such as a Button's
// contentColor. Passing an explicit colour still wins.
@Composable
fun ChaiTitle(
    titleText: String,
    modifier: Modifier = Modifier,
    titleColor: Color = Color.Unspecified,
) = Text(
    text = titleText,
    modifier = modifier,
    color = titleColor,
    style = MaterialTheme.typography.titleLarge,
    textAlign = TextAlign.Start,
)

@Composable
fun ChaiSubTitle(
    titleText: String,
    modifier: Modifier = Modifier,
    titleColor: Color = Color.Unspecified,
    textAlign: TextAlign? = TextAlign.Start,
) = Text(
    text = titleText,
    modifier = modifier,
    color = titleColor,
    style = MaterialTheme.typography.titleMedium,
    textAlign = textAlign,
)

@Composable
fun ChaiBodyXSmallBold(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    textAlign: TextAlign? = TextAlign.Start,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    textAlign = textAlign,
)

@Composable
fun ChaiBodyXSmall(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.labelMedium,
    textAlign = TextAlign.Start,
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
)

@Composable
fun ChaiBodySmallBold(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
    textAlign = TextAlign.Start,
    maxLines = maxLines,
)

@Composable
fun ChaiBodySmall(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.bodySmall,
    textAlign = textAlign,
    maxLines = maxLines,
    minLines = minLines,
    overflow = TextOverflow.Ellipsis,
)

@Composable
fun ChaiBodyMediumBold(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
    textAlign = textAlign,
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
)

@Composable
fun ChaiBodyMedium(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.bodyMedium,
    textAlign = TextAlign.Start,
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
)

@Composable
fun ChaiBodyLargeBold(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
    textAlign = TextAlign.Start,
    maxLines = maxLines,
    overflow = overflow,
)

@Composable
fun ChaiBodyLarge(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.bodyLarge,
    textAlign = TextAlign.Start,
)

@Composable
fun ChaiTextButtonLight(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
) = Text(
    text = bodyText.uppercase(),
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Light),
    textAlign = TextAlign.Start,
)

@Composable
fun CPrimaryButtonText(
    text: String,
    modifier: Modifier = Modifier,
    textAllCaps: Boolean = false,
    textColor: Color = Color.Unspecified,
) = Text(
    text = if (textAllCaps) text.uppercase() else text,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.labelLarge,
    textAlign = TextAlign.Center,
)

@Composable
fun ChaiTextLabelLarge(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.labelSmall,
    textAlign = textAlign,
)

@Composable
fun ChaiTextLabelMedium(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.labelSmall,
    textAlign = TextAlign.Start,
)

@Composable
fun ChaiTextLabelSmall(
    bodyText: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Light),
    textAlign = TextAlign.Start,
)