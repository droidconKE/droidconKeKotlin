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
package com.android254.presentation.feed.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.LoadingBox
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette

@Composable
fun FeedLoadingComponent() {
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.chaiColorsPalette.background)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        LoadingBox(height = 20.dp, widthRatio = 1.0f)
        Spacer(modifier = Modifier.height(10.dp))
        LoadingBox(height = 20.dp, widthRatio = 0.8f)
        Spacer(modifier = Modifier.height(10.dp))
        LoadingBox(height = 100.dp, widthRatio = 1.0f)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LoadingBox(height = 40.dp, width = 80.dp)
            LoadingBox(height = 20.dp, width = 50.dp)
        }
    }
}

@ChaiLightAndDarkComposePreview
@Composable
fun FeedLoadingComponentPreview() {
    ChaiDCKE22Theme {
        FeedLoadingComponent()
    }
}