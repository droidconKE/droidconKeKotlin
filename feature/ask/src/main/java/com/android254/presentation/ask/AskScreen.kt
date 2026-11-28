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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.adaptive.DroidconTooltip
import com.android254.presentation.common.adaptive.clickablePointer
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.components.SessionsLoadingCard
import com.android254.presentation.common.insets.DroidconWindowInsets
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ke.droidcon.kotlin.chai.R as ChaiR

@Composable
fun AskRoute(
    navigateBack: () -> Unit,
    navigateToSessionDetails: (String) -> Unit,
    viewModel: AskViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val surfaces by viewModel.surfaces.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    AskScreen(
        cards = AskSessionCards(sessions, navigateToSessionDetails),
        state = state,
        surfaces = surfaces.toImmutableList(),
        onBack = navigateBack,
        onQuestionChange = viewModel::onQuestionChange,
        onAsk = viewModel::ask,
    )
}

@Composable
internal fun AskScreen(
    cards: AskSessionCards,
    state: AskState,
    surfaces: ImmutableList<A2uiSurfaceModel>,
    onBack: () -> Unit,
    onQuestionChange: (String) -> Unit,
    onAsk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val ask = {
        keyboard?.hide()
        onAsk()
    }
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
                    enabled = !state.isAsking,
                    label = { Text(stringResource(R.string.ask_question_label)) },
                    placeholder = { Text(stringResource(R.string.ask_question_hint)) },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { ask() }),
                )
            }
            item(key = "ask") {
                AskButton(isAsking = state.isAsking, canAsk = state.question.isNotBlank(), onAsk = ask)
            }
            if (state.failed) {
                item(key = "failed") {
                    Text(
                        text = stringResource(R.string.ask_failed),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }
            when {
                state.isAsking ->
                    items(count = LOADING_CARDS, key = { "loading_$it" }, contentType = { "loading" }) {
                        SessionsLoadingCard(modifier = Modifier.animateItem())
                    }
                surfaces.isEmpty() ->
                    item(key = "suggestions") {
                        AskSuggestions(
                            onPick = { question ->
                                onQuestionChange(question)
                                ask()
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                else ->
                    items(surfaces, key = { it.id }) { surface ->
                        CompositionLocalProvider(LocalAskSessionCards provides cards) {
                            A2uiSurface(surfaceModel = surface, modifier = Modifier.fillMaxWidth().animateItem())
                        }
                    }
            }
        }
    }
}

@Composable
private fun AskButton(
    isAsking: Boolean,
    canAsk: Boolean,
    onAsk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Stays enabled while asking, like the ticket form, so the loading indicator keeps the button's colours.
    Button(
        onClick = { if (!isAsking) onAsk() },
        enabled = isAsking || canAsk,
        shapes = ButtonDefaults.shapes(),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (isAsking) {
            LoadingIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(ButtonDefaults.IconSize))
            Text(text = stringResource(R.string.ask_asking), modifier = Modifier.padding(start = ButtonDefaults.IconSpacing))
        } else {
            Text(stringResource(R.string.ask_button))
        }
    }
}

@Composable
private fun AskSuggestions(
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val suggestions =
        persistentListOf(
            stringResource(R.string.ask_suggestion_after_lunch),
            stringResource(R.string.ask_suggestion_compose),
            stringResource(R.string.ask_suggestion_ai),
        )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.ask_suggestions_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { suggestion ->
                SuggestionChip(onClick = { onPick(suggestion) }, label = { Text(suggestion) })
            }
        }
    }
}

private const val LOADING_CARDS = 3

@ChaiLightAndDarkComposePreviews
@Composable
private fun AskScreenAskingPreview() {
    ChaiTheme {
        AskScreen(
            cards = AskSessionCards(),
            state = AskState(question = "What is on after lunch on the last day?", isAsking = true),
            surfaces = persistentListOf(),
            onBack = {},
            onQuestionChange = {},
            onAsk = {},
        )
    }
}

@ChaiLightAndDarkComposePreviews
@Composable
private fun AskScreenEmptyPreview() {
    ChaiTheme {
        AskScreen(
            cards = AskSessionCards(),
            state = AskState(),
            surfaces = persistentListOf(),
            onBack = {},
            onQuestionChange = {},
            onAsk = {},
        )
    }
}