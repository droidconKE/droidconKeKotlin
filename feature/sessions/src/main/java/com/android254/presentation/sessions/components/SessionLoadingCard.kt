/*
 * Copyright 2023 DroidconKE
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
package com.android254.presentation.sessions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.AnimatedShimmerEffect
import com.android254.presentation.common.components.LoadingBox
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SessionsLoadingCard(modifier: Modifier = Modifier) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.chaiColorsPalette.cardsBackground),
    ) {
        AnimatedShimmerEffect(
            gradientColors =
                persistentListOf(
                    MaterialTheme.chaiColorsPalette.loadingStateOnCardsColor.copy(alpha = 0.3f),
                    MaterialTheme.chaiColorsPalette.loadingStateOnCardsColor.copy(alpha = 0.2f),
                    MaterialTheme.chaiColorsPalette.loadingStateOnCardsColor.copy(alpha = 0.3f),
                ),
        ) { brush ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LoadingBox(height = 20.dp, widthRatio = 0.7f, brush = brush)
                    LoadingBox(height = 32.dp, width = 32.dp, brush = brush, cornerRadius = 16.dp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LoadingBox(height = 20.dp, width = 60.dp, brush = brush)
                    LoadingBox(height = 20.dp, width = 60.dp, brush = brush)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LoadingBox(height = 30.dp, width = 30.dp, brush = brush, cornerRadius = 15.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    LoadingBox(height = 16.dp, width = 100.dp, brush = brush)
                }
                Spacer(modifier = Modifier.height(12.dp))

                LoadingBox(height = 1.dp, widthRatio = 1f, brush = brush)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    LoadingBox(height = 14.dp, width = 80.dp, brush = brush)
                    LoadingBox(height = 14.dp, width = 80.dp, brush = brush)
                }
            }
        }
    }
}

@ChaiLightAndDarkComposePreviews
@Composable
private fun SessionsLoadingComponentPreview() {
    ChaiTheme {
        SessionsLoadingCard()
    }
}