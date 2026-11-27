/*
 * Copyright 2026 DroidconKE
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
package com.android254.presentation.ask

import androidx.a2ui.model.processor.A2uiSurfaceModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.adaptive.DroidconTooltip
import com.android254.presentation.common.adaptive.clickablePointer
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.insets.DroidconWindowInsets
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ke.droidcon.kotlin.chai.R as ChaiR

@Composable
fun AskRoute(
    navigateBack: () -> Unit,
    viewModel: AskViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val surfaces by viewModel.surfaces.collectAsStateWithLifecycle()
    AskScreen(
        state = state,
        surfaces = surfaces.toImmutableList(),
        onBack = navigateBack,
        onQuestionChange = viewModel::onQuestionChange,
        onAsk = viewModel::ask,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AskScreen(
    state: AskState,
    surfaces: ImmutableList<A2uiSurfaceModel>,
    onBack: () -> Unit,
    onQuestionChange: (String) -> Unit,
    onAsk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { Text(stringResource(R.string.ask_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    val backLabel = stringResource(R.string.back_arrow_icon_description)
                    DroidconTooltip(text = backLabel) {
                        IconButton(onClick = onBack, modifier = Modifier.clickablePointer()) {
                            Icon(painter = painterResource(ChaiR.drawable.ic_back_arrow), contentDescription = backLabel)
                        }
                    }
                },
            )
        },
        contentWindowInsets = DroidconWindowInsets.screenContent,
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(padding)
                    .readablePaneWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "question") {
                OutlinedTextField(
                    value = state.question,
                    onValueChange = onQuestionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.ask_question_label)) },
                    placeholder = { Text(stringResource(R.string.ask_question_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onAsk() }),
                )
            }
            item(key = "ask") {
                Button(onClick = onAsk, enabled = !state.isAsking && state.question.isNotBlank()) {
                    Text(stringResource(R.string.ask_button))
                }
            }
            if (state.isAsking) {
                item(key = "asking") { CircularProgressIndicator(modifier = Modifier.padding(8.dp)) }
            }
            if (state.failed) {
                item(key = "failed") {
                    Text(
                        text = stringResource(R.string.ask_failed),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            items(surfaces, key = { it.id }) { surface ->
                A2uiSurface(surfaceModel = surface, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}