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
package com.android254.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.models.SessionPresentationModel
import com.droidconke.chai.atoms.*
import com.droidconke.chai.atoms.MontserratBold
import com.droidconke.chai.atoms.MontserratMedium
import com.droidconke.chai.atoms.MontserratRegular
import com.droidconke.chai.chaiColorsPalette
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeSessionSection(
    modifier: Modifier = Modifier,
    sessions: List<SessionPresentationModel>,
    onSessionClick: (SessionPresentationModel) -> Unit,
    onViewAllSessionClicked: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        HomeSectionHeader(
            sectionLabel = stringResource(id = R.string.sessions_label),
            sectionSize = sessions.size,
            onViewAllClicked = onViewAllSessionClicked
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.testTag("sessions")
        ) {
            items(sessions) { session ->
                HomeSessionContent(
                    session = session,
                    onSessionClick = onSessionClick
                )
            }
        }
    }
}

@Composable
fun HomeSectionHeader(
    sectionLabel: String,
    sectionSize: Int,
    onViewAllClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(vertical = 16.dp)
            .testTag("sectionHeader")
    ) {
        Text(
            text = sectionLabel,
            textAlign = TextAlign.Start,
            style = TextStyle(
                color = ChaiBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 25.sp,
                fontFamily = MontserratBold
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onViewAllClicked() }
                .testTag("viewAll")
        ) {
            Text(
                text = stringResource(id = R.string.view_all_label),
                textAlign = TextAlign.Start,
                style = TextStyle(
                    //made the color for the sessions dynamic for Dark mode visibility
                    color = if (isSystemInDarkTheme()) Color.White else ChaiBlue,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontFamily = MontserratMedium
                ),
                color = MaterialTheme.chaiColorsPalette.textColorPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .height(22.dp)
                    .width(34.dp)
                    .background(
                        color = colorResource(id = R.color.light_blue),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Text(
                    text = stringResource(id = R.string.format_plus_label, sectionSize),
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(
                        color = if (isSystemInDarkTheme()) colorResource(id = R.color.white) else colorResource(id = R.color.blue),
                        fontSize = 10.sp,
                        fontFamily = MontserratRegular
                    )
                )
            }
        }
    }
}

@Composable
fun HomeSessionContent(
    session: SessionPresentationModel,
    onSessionClick: (SessionPresentationModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(300.dp)
            .height(intrinsicSize = IntrinsicSize.Max),
        colors = CardDefaults.cardColors(containerColor = ChaiLightGrey),
        onClick = { onSessionClick(session) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth()
        ){
            Image(
                painter = painterResource(R.drawable.sessions_background),
                contentDescription = "background image",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = session.title,
                    modifier = Modifier.padding(25.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth()
                ){
                    AsyncImage(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(session.speakerImage)
                            .build(),
                        contentDescription = stringResource(id = R.string.sessions_label),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = session.speakerName)
                }
                

            }
        }

        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .height(80.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = session.title,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    color = ChaiBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = MontserratBold
                ),
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "@ ${session.startTime} | ${session.venue} ",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    color = ChaiSmokeyGrey,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontFamily = MontserratRegular
                )
            )
        }
    }
}



