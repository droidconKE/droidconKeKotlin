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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.droidconke.chai.atoms.*
import com.droidconke.chai.atoms.MontserratRegular
import com.droidconke.chai.atoms.MontserratThin
import com.droidconke.chai.chaiColorsPalette

/**
 * CText:
 * Typography( From  is the art of arranging letters and text in a way that makes the copy legible,
 * clear, and visually appealing to the reader.
 * It involves font style, appearance, and structure, which aims to elicit certain emotions and convey specific messages.
 * In short, typography is what brings the text to life.
 *
 * This is a shorter approach where our theme will not require a specific font BUT will use CText as a file to construct
 * our text. this is a shorter approach for making a Design system type. For a longer version see this repo:
 * [KahawaLove](https://github.com/tamzi/KahawaLove)
 *
 * */

/**
 * Title based fonts
 * */
@Composable
fun CParagraph(dParagraph: String) {
    Text(
        text = dParagraph,
        style = TextStyle(
            color = ChaiBlack,
            fontSize = 12.sp,
            fontWeight = FontWeight.W500,
            fontFamily = MontserratRegular
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CPageTitle(pageTitle: String) {
    Text(
        text = pageTitle,
        style = TextStyle(
            color = ChaiBlue,
            fontSize = 33.sp,
            fontWeight = FontWeight.W300,
            fontFamily = MontserratThin

        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CSubtitle(dSubtitle: String) {
    Text(
        text = dSubtitle,
        style = TextStyle(
            color = ChaiRed,
            fontSize = 15.sp,
            fontWeight = FontWeight.W700,
            fontFamily = MontserratRegular

        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CActionText(cAction: String) {
    Text(
        text = cAction,
        style = TextStyle(
            color = ChaiRed,
            fontSize = 15.sp,
            fontWeight = FontWeight.W700,
            fontFamily = MontserratRegular

        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ChaiTitle(
    modifier: Modifier = Modifier,
    titleText: String,
    titleColor: Color = Color.Unspecified
) {
    Text(
        modifier = modifier,
        text = titleText,
        style = TextStyle(
            color = titleColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.W700,
            fontFamily = MontserratBold
        ),
        textAlign = TextAlign.Start
    )
}

@Composable
fun ChaiSubTitle(
    modifier: Modifier = Modifier,
    titleText: String,
    titleColor: Color = Color.Unspecified,
    textAlign: TextAlign? = TextAlign.Start
) {
    Text(
        modifier = modifier,
        text = titleText,
        style = TextStyle(
            color = titleColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.W700,
            fontFamily = MontserratBold,
            lineHeight = 22.sp
        ),
        textAlign = textAlign
    )
}

@Composable
fun ChaiBodyXSmallBold(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified,
    textAlign: TextAlign? = TextAlign.Start
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.W500,
            fontFamily = MontserratMedium,
            lineHeight = 16.sp
        ),
        textAlign = textAlign
    )
}

@Composable
fun ChaiBodyXSmall(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.W400,
            fontFamily = MontserratRegular,
            lineHeight = 16.sp
        ),
        textAlign = TextAlign.Start
    )
}

@Composable
fun ChaiBodySmallBold(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.W700,
            fontFamily = MontserratBold,
            lineHeight = 20.sp
        ),
        textAlign = TextAlign.Start,
        maxLines = maxLines
    )
}

@Composable
fun ChaiBodySmall(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.W400,
            fontFamily = MontserratRegular,
            lineHeight = 20.sp
        ),
        textAlign = textAlign,
        maxLines = maxLines,
        minLines = minLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun ChaiBodyMediumBold(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.W600,
            fontFamily = MontserratSemiBold,
            lineHeight = 20.sp
        ),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun ChaiBodyMedium(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            fontFamily = MontserratRegular,
            lineHeight = 20.sp
        ),
        textAlign = TextAlign.Start,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun ChaiBodyLargeBold(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.W600,
            fontFamily = MontserratSemiBold,
            lineHeight = 22.sp
        ),
        textAlign = TextAlign.Start
    )
}

@Composable
fun ChaiBodyLarge(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.W400,
            fontFamily = MontserratRegular,
            lineHeight = 22.sp
        ),
        textAlign = TextAlign.Start
    )
}

@Composable
fun ChaiTextButtonLight(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified
) {
    Text(
        modifier = modifier,
        text = bodyText.uppercase(),
        style = TextStyle(
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.W300,
            fontFamily = MontserratLight,
            lineHeight = 16.sp
        ),
        textAlign = TextAlign.Start
    )
}

@Composable
fun CPrimaryButtonText(
    modifier: Modifier = Modifier,
    text: String,
    textAllCaps: Boolean = false,
    textColor: Color = MaterialTheme.chaiColorsPalette.textButtonColor
) {
    Text(
        modifier = modifier,
        text = if (textAllCaps) text.uppercase() else text,
        style = TextStyle(
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.W600,
            fontFamily = MontserratSemiBold,
            lineHeight = 22.sp
        ),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ChaiTextLabelLarge(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.W400,
            fontFamily = MontserratRegular,
            lineHeight = 16.sp
        ),
        textAlign = textAlign
    )
}

@Composable
fun ChaiTextLabelMedium(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.W400,
            fontFamily = MontserratRegular,
            lineHeight = 14.sp
        ),
        textAlign = TextAlign.Start
    )
}

@Composable
fun ChaiTextLabelSmall(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified
) {
    Text(
        modifier = modifier,
        text = bodyText,
        style = TextStyle(
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.W300,
            fontFamily = MontserratLight,
            lineHeight = 14.sp
        ),
        textAlign = TextAlign.Start
    )
}