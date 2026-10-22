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
package com.android254.presentation.ticket.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.components.EmptyStatePanel
import com.android254.presentation.common.insets.DroidconWindowInsets
import com.android254.presentation.ticket.AddTicketForm
import com.android254.presentation.ticket.ScanResult
import com.android254.presentation.ticket.TicketAction
import com.android254.presentation.ticket.TicketUi
import com.android254.presentation.ticket.TicketUiState
import com.android254.presentation.ticket.TicketViewModel
import com.android254.presentation.ticket.scanTicketCode
import com.droidconke.chai.atoms.LARGE_FONT_SCALE
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R
import kotlinx.coroutines.launch
import ke.droidcon.kotlin.chai.R as ChaiR

@Composable
fun TicketRoute(
    navigateBack: () -> Unit,
    viewModel: TicketViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    TicketScreen(
        state = state,
        onBack = navigateBack,
        onAction = { action ->
            if (action == TicketAction.Scan) {
                scope.launch {
                    when (val result = scanTicketCode(context)) {
                        is ScanResult.Code -> viewModel.onScanned(result.value)
                        ScanResult.Unavailable -> viewModel.onScannerUnavailable()
                        ScanResult.Cancelled -> Unit
                    }
                }
            } else {
                viewModel.onAction(action)
            }
        },
    )
}

@Composable
internal fun TicketScreen(
    state: TicketUiState,
    onBack: () -> Unit,
    onAction: (TicketAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ticket = (state as? TicketUiState.Ready)?.ticket
    var confirmingRemoval by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        topBar = { TicketTopBar(onBack = onBack, onRemove = { confirmingRemoval = true }.takeIf { ticket != null }) },
        contentWindowInsets = DroidconWindowInsets.screenContent,
    ) { padding ->
        val content =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        when {
            state !is TicketUiState.Ready -> Box(content)
            ticket == null -> AddFirstTicket(state.form, onAction, content)
            else -> Pass(ticket, content)
        }
    }
    if (confirmingRemoval && ticket != null) {
        AlertDialog(
            onDismissRequest = { confirmingRemoval = false },
            title = { Text(stringResource(R.string.ticket_remove_title, ticket.number)) },
            text = { Text(stringResource(R.string.ticket_remove_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(TicketAction.Remove)
                        confirmingRemoval = false
                    },
                ) { Text(stringResource(R.string.ticket_remove)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingRemoval = false }) { Text(stringResource(R.string.ticket_cancel)) }
            },
        )
    }
}

@Composable
private fun TicketTopBar(
    onBack: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    var menuOpen by remember { mutableStateOf(false) }
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        title = { Text(stringResource(R.string.ticket_title), style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(ChaiR.drawable.ic_back_arrow),
                    contentDescription = stringResource(R.string.back_arrow_icon_description),
                )
            }
        },
        actions = {
            if (onRemove != null) {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(imageVector = ChaiIcons.MoreVert, contentDescription = stringResource(R.string.ticket_more_actions))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.ticket_remove_ticket)) },
                        onClick = {
                            menuOpen = false
                            onRemove()
                        },
                    )
                }
            }
        },
    )
}

/** The pass fills the screen, as a ticket does; nothing to scroll past at the door. Large text gets a scrolling pass instead of a tiny code. */
@Composable
private fun Pass(
    ticket: TicketUi,
    modifier: Modifier = Modifier,
) {
    if (ticket.status.showsCode) GateScreenMode()
    val entrance = remember { MutableTransitionState(false).apply { targetState = true } }
    val largeFont = LocalDensity.current.fontScale > LARGE_FONT_SCALE
    Box(
        modifier = if (largeFont) modifier.verticalScroll(rememberScrollState()) else modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visibleState = entrance,
            enter =
                slideInVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) { it / 4 } +
                    fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
        ) {
            TicketPass(
                ticket = ticket,
                fitHeight = !largeFont,
                modifier =
                    Modifier
                        .readablePaneWidth()
                        .then(if (largeFont) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun AddFirstTicket(
    form: AddTicketForm,
    onAction: (TicketAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier =
                Modifier
                    .readablePaneWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyStatePanel(
                icon = rememberVectorPainter(ChaiIcons.ConfirmationNumber),
                message = stringResource(R.string.ticket_add_title),
                body = stringResource(R.string.ticket_add_body),
            )
            AddTicketPanel(form = form, onAction = onAction)
        }
    }
}