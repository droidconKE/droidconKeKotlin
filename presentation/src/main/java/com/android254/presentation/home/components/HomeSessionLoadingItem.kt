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
package com.android254.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.LoadingBox
import com.droidconke.chai.ChaiDCKE22Theme

@Composable
fun HomeSessionLoadingItem() {
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        LoadingBox(height = 120.dp, width = 200.dp)
        Spacer(modifier = Modifier.height(10.dp))
        LoadingBox(height = 20.dp, width = 200.dp)
        Spacer(modifier = Modifier.height(10.dp))
        LoadingBox(height = 20.dp, width = 170.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeSessionLoadingItemPreview() {
    ChaiDCKE22Theme {
        HomeSessionLoadingItem()
    }
}