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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.common.theme.Montserrat
import com.droidconke.chai.components.CButton
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
                            titleContentColor = if (darkTheme) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                Color(
                                    0xFF1B1B1F
                                )
                            },
                            navigationIconContentColor = if (darkTheme) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                Color(
                                    0xFF1B1B1F
                                )
                            },
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
                        titleContentColor = if (darkTheme) Color(0xFFF2F0F4) else MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = if (darkTheme) Color(0xFFF2F0F4) else MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
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
            Text(
                text = stringResource(R.string.feedback_improve_label),
                style = TextStyle(
                    fontWeight = FontWeight.ExtraBold,
                    color = colorResource(id = R.color.blue),
                    fontSize = 18.sp,
                    fontFamily = Montserrat
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = stringResource(R.string.feedback_event_label))
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Card {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(40.dp),
                                    painter = painterResource(id = R.drawable.ic_feedback_bad_face),
                                    contentDescription = stringResource(id = R.string.sign_in_label)
                                )
                                Text(
                                    text = stringResource(R.string.Bad)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Card {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(40.dp),
                                    painter = painterResource(id = R.drawable.ic_feedback_neutral_face),
                                    contentDescription = stringResource(id = R.string.sign_in_label)
                                )
                                Text(
                                    text = stringResource(R.string.Okay)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Card {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(40.dp),
                                    painter = painterResource(id = R.drawable.ic_feedback_smiling_face),
                                    contentDescription = stringResource(id = R.string.sign_in_label)
                                )
                                Text(
                                    text = stringResource(R.string.Great)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(stringResource(R.string.feedback_type_message_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(120.dp),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(7.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            CButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag("submit_feedback_button"),
                isEnabled = true,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (darkTheme) Color.Black else colorResource(id = R.color.blue),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.feedback_label).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun FeedbackTitle() {
    Text(
        stringResource(R.string.feedback_label),
        modifier = Modifier.testTag("heading")
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
    DroidconKE2023Theme {
        FeedBackScreen(darkTheme = isSystemInDarkTheme())
    }
}