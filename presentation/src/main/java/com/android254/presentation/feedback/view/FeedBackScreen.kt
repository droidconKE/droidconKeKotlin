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
package com.android254.presentation.feedback.view

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiGrey90
import com.droidconke.chai.atoms.ChaiLightGrey
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.CButton
import com.droidconke.chai.components.ChaiBodyLarge
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiBodySmall
import com.droidconke.chai.components.ChaiBodyXSmallBold
import com.droidconke.chai.components.ChaiSubTitle
import ke.droidcon.kotlin.presentation.R

@Composable
fun FeedBackRoute(
    darkTheme: Boolean = isSystemInDarkTheme(),
    navigateBack: () -> Unit = {}
) {
    FeedBackScreen(
        darkTheme = darkTheme,
        navigateBack = navigateBack
    )
}

@Composable
private fun FeedBackScreen(
    darkTheme: Boolean,
    navigateBack: () -> Unit = {}
) {
    var value by remember {
        mutableStateOf("")
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val isCollapsed = remember { derivedStateOf { scrollBehavior.state.collapsedFraction > 0.7 } }

    Scaffold(
        topBar = {
            Box {
                if (isCollapsed.value) {
                    TopAppBar(
                        title = { FeedbackTitle() },
                        navigationIcon = {
                            IconButton(
                                onClick = { navigateBack() }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_back_arrow),
                                    contentDescription = stringResource(R.string.back_arrow_icon_description)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            titleContentColor = ChaiWhite,
                            navigationIconContentColor = ChaiWhite,
                            containerColor = Color.Transparent
                        )
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth(),
                        painter = if (darkTheme) {
                            painterResource(R.drawable.toolbar_bg_sign_up_dark)
                        } else {
                            painterResource(R.drawable.topbar_bg_sign_up)
                        },
                        contentDescription = stringResource(R.string.login_screen_bg_image_description),
                        contentScale = ContentScale.FillBounds
                    )
                }
                LargeTopAppBar(
                    title = { FeedbackTitle() },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigateBack() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back_arrow),
                                contentDescription = stringResource(R.string.back_arrow_icon_description)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = ChaiWhite,
                        navigationIconContentColor = ChaiWhite
                    )
                )
            }
        },
        containerColor = MaterialTheme.chaiColorsPalette.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            ChaiSubTitle(
                titleText = stringResource(R.string.feedback_improve_label),
                titleColor = MaterialTheme.chaiColorsPalette.textLabelAndHeadings
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .background(
                        color = MaterialTheme.chaiColorsPalette.cardsBackground,
                        shape = RoundedCornerShape(8.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                ChaiBodySmall(
                    bodyText = stringResource(R.string.feedback_event_label),
                    textColor = MaterialTheme.chaiColorsPalette.textNormalColor
                )
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier
                            .background(color = ChaiLightGrey, shape = RoundedCornerShape(4.dp))
                            .size(68.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier
                                .height(40.dp)
                                .width(40.dp),
                            painter = painterResource(id = R.drawable.ic_feedback_bad_face),
                            contentDescription = stringResource(id = R.string.Bad)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        ChaiBodyXSmallBold(
                            bodyText = stringResource(R.string.Bad),
                            textColor = ChaiGrey90
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(
                        modifier = Modifier
                            .background(color = ChaiLightGrey, shape = RoundedCornerShape(4.dp))
                            .size(68.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier
                                .height(40.dp)
                                .width(40.dp),
                            painter = painterResource(id = R.drawable.ic_feedback_neutral_face),
                            contentDescription = stringResource(id = R.string.Okay)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        ChaiBodyXSmallBold(
                            bodyText = stringResource(R.string.Okay),
                            textColor = ChaiGrey90
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(
                        modifier = Modifier
                            .background(color = ChaiLightGrey, shape = RoundedCornerShape(4.dp))
                            .size(68.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier
                                .height(40.dp)
                                .width(40.dp),
                            painter = painterResource(id = R.drawable.ic_feedback_smiling_face),
                            contentDescription = stringResource(id = R.string.Great)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        ChaiBodyXSmallBold(
                            bodyText = stringResource(R.string.Great),
                            textColor = ChaiGrey90
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(120.dp),
                value = value,
                onValueChange = { value = it },
                label = {
                    ChaiBodySmall(bodyText = stringResource(R.string.feedback_type_message_label))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.chaiColorsPalette.textFieldBackgroundColor,
                    unfocusedContainerColor = MaterialTheme.chaiColorsPalette.textFieldBackgroundColor,
                    disabledContainerColor = MaterialTheme.chaiColorsPalette.textFieldBackgroundColor,
                    focusedBorderColor = MaterialTheme.chaiColorsPalette.textFieldBorderColor,
                    unfocusedBorderColor = MaterialTheme.chaiColorsPalette.textFieldBorderColor
                ),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            CButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(48.dp)
                    .testTag("submit_feedback_button"),
                isEnabled = true,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.chaiColorsPalette.primary,
                    contentColor = Color.White
                )
            ) {
                ChaiBodyMediumBold(
                    bodyText = stringResource(R.string.feedback_button).uppercase()
                )
            }
        }
    }
}

@Composable
fun FeedbackTitle() {
    ChaiBodyLarge(
        modifier = Modifier.testTag("heading"),
        bodyText = stringResource(R.string.feedback_label)
    )
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun FeedBackScreenPreview() {
    ChaiDCKE22Theme {
        FeedBackScreen(darkTheme = isSystemInDarkTheme())
    }
}