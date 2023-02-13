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
package com.android254.presentation.about.view

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.theme.DroidconKE2022Theme
import com.android254.presentation.models.OrganizingTeamMember
import com.droidconke.chai.atoms.ChaiDarkerGrey
import com.droidconke.chai.atoms.ChaiSmokeyGrey
import com.droidconke.chai.atoms.ChaiTeal
import com.droidconke.chai.atoms.MontserratRegular
import com.android254.droidconKE2023.presentation.R

@Composable
fun OrganizingTeamComponent(
    modifier: Modifier = Modifier,
    teamMember: OrganizingTeamMember,
    onClickMember: (Int) -> Unit,
) {
    Column(modifier = modifier.clickable(onClick = { onClickMember(teamMember.id) }),
        horizontalAlignment = Alignment.CenterHorizontally) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(teamMember.image).build(),
            placeholder = painterResource(R.drawable.droidcon_icon),
            contentDescription = "Member profile",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(99.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, ChaiTeal, RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.height(6.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = teamMember.name,
            style = TextStyle(
                color = ChaiDarkerGrey,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontFamily = MontserratRegular,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(2.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = teamMember.desc,
            style = TextStyle(
                color = ChaiSmokeyGrey,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontFamily = MontserratRegular,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
fun Preview() {
    DroidconKE2022Theme {
        OrganizingTeamComponent(modifier = Modifier, teamMember = OrganizingTeamMember(
            name = "Frank Tamre",
            desc = "Main Man",
            image = "https://media-exp1.licdn.com/dms/image/C4D03AQGn58utIO-x3w/profile-displayphoto-shrink_200_200/0/1637478114039?e=2147483647&v=beta&t=3kIon0YJQNHZojD3Dt5HVODJqHsKdf2YKP1SfWeROnI"
        ), onClickMember = {})
    }
}