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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.android254.presentation.ticket.AddTicketError
import com.android254.presentation.ticket.AddTicketForm
import com.android254.presentation.ticket.TicketAction
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R

@Composable
internal fun AddTicketPanel(
    form: AddTicketForm,
    onAction: (TicketAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val noMatch = form.error?.takeIf { it == AddTicketError.NoMatch }?.let { stringResource(it.message()) }
        OutlinedTextField(
            value = form.number,
            onValueChange = { onAction(TicketAction.NumberChanged(it)) },
            label = { Text(stringResource(R.string.ticket_number_label)) },
            placeholder = { Text(stringResource(R.string.ticket_number_placeholder)) },
            singleLine = true,
            isError = noMatch != null,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                ),
            modifier = Modifier.fillMaxWidth().fieldError(noMatch),
        )
        OutlinedTextField(
            value = form.email,
            onValueChange = { onAction(TicketAction.EmailChanged(it)) },
            label = { Text(stringResource(R.string.ticket_email_label)) },
            singleLine = true,
            isError = noMatch != null,
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(TicketAction.Add) }),
            modifier = Modifier.fillMaxWidth().fieldError(noMatch),
        )
        form.error?.let { error ->
            Text(
                text = stringResource(error.message()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        Button(
            onClick = { onAction(TicketAction.Add) },
            enabled = form.canSubmit,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (form.isAdding) {
                LoadingIndicator(modifier = Modifier.size(ButtonDefaults.IconSize))
                Text(
                    text = stringResource(R.string.ticket_adding),
                    modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
                )
            } else {
                Text(stringResource(R.string.ticket_add_action))
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.ticket_or),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        OutlinedButton(
            onClick = { onAction(TicketAction.Scan) },
            enabled = !form.isAdding,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = ChaiIcons.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Text(
                text = stringResource(R.string.ticket_scan_action),
                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
            )
        }
    }
}

private fun Modifier.fieldError(message: String?): Modifier = if (message == null) this else semantics { error(message) }

private fun AddTicketError.message(): Int =
    when (this) {
        AddTicketError.NoMatch -> R.string.ticket_error_no_match
        AddTicketError.NotATicket -> R.string.ticket_error_not_a_ticket
        AddTicketError.Offline -> R.string.ticket_error_offline
        AddTicketError.Busy -> R.string.ticket_error_busy
        AddTicketError.Failed -> R.string.ticket_error_failed
        AddTicketError.ScannerUnavailable -> R.string.ticket_error_scanner
    }

@ChaiLightAndDarkComposePreviews
@Composable
private fun AddTicketPanelPreview() {
    ChaiTheme {
        Surface {
            AddTicketPanel(
                form = AddTicketForm(number = "TIC-70253", email = "magak@example.com", error = AddTicketError.NoMatch),
                onAction = {},
            )
        }
    }
}