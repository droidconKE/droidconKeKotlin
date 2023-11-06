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
package com.android254.presentation.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.droidconke.chai.ChaiDCKE22Theme
import ke.droidcon.kotlin.presentation.R

@Composable
fun DroidconAppBar(
    modifier: Modifier = Modifier,
    onActionClicked: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(start = 20.dp, end = 20.dp, top = 19.dp, bottom = 15.dp)
            .testTag("droidcon_topBar_notSignedIn"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.droidcon_logo_dark else R.drawable.droidcon_logo),
            contentDescription = stringResource(id = R.string.logo)
        )
        Spacer(modifier = Modifier.weight(1f))

//        Image(
//            painter = painterResource(id = R.drawable.whilte_padlock),
//            contentDescription = null,
//            modifier = Modifier
//                .background(
//                    color = ChaiTeal,
//                    shape = CircleShape
//                )
//                .width(30.dp)
//                .height(30.dp)
//                .padding(8.dp)
//                .clickable { onActionClicked() }
//        )
    }
}

@Preview
@Composable
fun DroidconAppBarPreview() {
    ChaiDCKE22Theme {
        DroidconAppBar()
    }
}