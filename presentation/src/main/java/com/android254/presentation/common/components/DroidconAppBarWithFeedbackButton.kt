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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.droidconke.chai.atoms.ChaiCoal
import com.droidconke.chai.atoms.ChaiTeal
import com.droidconke.chai.atoms.MontserratRegular
import ke.droidcon.kotlin.presentation.R

@Composable
fun DroidconAppBarWithFeedbackButton(
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit,
    userProfile: String,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(start = 20.dp, end = 20.dp, top = 19.dp, bottom = 15.dp)
            .testTag("droidcon_topBar_with_Feedback"),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(id = R.drawable.droidcon_logo),
            contentDescription = stringResource(id = R.string.logo)
        )
        Spacer(modifier = Modifier.weight(1f))

        FeedbackButton(onButtonClick = onButtonClick)

        Spacer(modifier = Modifier.width(15.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(userProfile)
                .build(),
            placeholder = painterResource(R.drawable.droidcon_icon),
            contentDescription = stringResource(id = R.string.user_profile),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape),
        )
    }
}

@Composable
fun FeedbackButton(
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                color = ChaiTeal.copy(alpha = 0.21f),
            )
            .clickable(onClick = onButtonClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_feedback_emoji),
            contentDescription = null
        )

        Text(
            modifier = Modifier,
            text = stringResource(id = R.string.feedback),
            style = TextStyle(
                color = ChaiCoal,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontFamily = MontserratRegular,
            ),
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_send_icon),
            contentDescription = null,
            tint = ChaiTeal,
        )
    }
}

@Preview
@Composable
fun Preview() {
    DroidconKE2023Theme {
        DroidconAppBarWithFeedbackButton(
            onButtonClick = {},
            userProfile = "https://media-exp1.licdn.com/dms/image/C4D03AQGn58utIO-x3w/profile-displayphoto-shrink_200_200/0/1637478114039?e=2147483647&v=beta&t=3kIon0YJQNHZojD3Dt5HVODJqHsKdf2YKP1SfWeROnI"
        )
    }
}