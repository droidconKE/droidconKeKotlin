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
package com.android254.presentation.auth.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidconke.chai.atoms.ChaiTeal
import com.droidconke.chai.atoms.ChaiWhite

@Composable
fun GoogleSignInButton(
    text: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    loadingText: String = "Signing in...",
    isLoading: Boolean = false,
    borderColor: Color = Color.LightGray,
    backgroundColor: Color = ChaiWhite,
    progressIndicatorColor: Color = ChaiTeal,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable(
                enabled = !isLoading,
                onClick = onClick
            )
            .shadow(elevation = 2.dp),
        border = BorderStroke(width = 1.dp, color = borderColor),
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 0.dp,
                    end = 8.dp,
                    top = 0.dp,
                    bottom = 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = "SignInButton",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(24.dp))

            Text(
                text = if (isLoading) loadingText else text,
                color = Color.Black,
                fontSize = 14.sp
            )
            if (isLoading) {
                Spacer(modifier = Modifier.width(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(16.dp)
                        .width(16.dp),
                    strokeWidth = 2.dp,
                    color = progressIndicatorColor
                )
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}