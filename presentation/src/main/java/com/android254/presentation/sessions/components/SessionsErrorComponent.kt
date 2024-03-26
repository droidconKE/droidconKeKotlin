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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.CPrimaryButtonText
import com.droidconke.chai.components.ChaiBodyMediumBold
import ke.droidcon.kotlin.presentation.R

@Composable
fun SessionsErrorComponent(
    errorMessage: String,
    retry: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChaiBodyMediumBold(
            bodyText = errorMessage,
            textColor = MaterialTheme.chaiColorsPalette.textNormalColor
        )
        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Button(onClick = { retry() }) {
            CPrimaryButtonText(
                text = stringResource(R.string.retry_label)
            )
        }
    }
}

@Preview
@Composable
fun SessionsErrorComponentPreview() {
    Surface(color = Color.White) {
        SessionsErrorComponent(errorMessage = "Something Went Wrong")
    }
}