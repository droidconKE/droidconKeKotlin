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
package com.android254.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.domain.models.Ticket
import com.android254.presentation.home.viewstate.HomeTicket
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R

@Composable
internal fun HomeTicketCard(
    ticket: HomeTicket,
    prominent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (ticket) {
        HomeTicket.Hidden -> Unit
        HomeTicket.Add -> AddTicketCard(onClick, modifier)
        is HomeTicket.Saved -> SavedTicketCard(ticket, prominent, onClick, modifier)
    }
}

@Composable
private fun AddTicketCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TicketBadge(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.ticket_add_title),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.home_ticket_add_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SavedTicketCard(
    ticket: HomeTicket.Saved,
    prominent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.chaiColorsPalette
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        color = if (prominent) palette.heroContainerColor else colors.surfaceContainer,
        contentColor = if (prominent) palette.heroOnContainerColor else colors.onSurface,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.ticket_title).uppercase(),
                    style = MaterialTheme.typography.labelSmallEmphasized,
                    color = if (prominent) palette.heroContentColor else colors.primary,
                )
                Text(
                    text = ticket.attendeeName,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                Text(
                    text = ticket.summary(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (prominent) palette.heroOnContainerColor else colors.onSurfaceVariant,
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (prominent) {
                    TicketBadge(palette.heroOnContainerColor, palette.heroContainerColor)
                } else {
                    TicketBadge(colors.primaryContainer, colors.onPrimaryContainer)
                }
                Text(
                    text = stringResource(R.string.home_ticket_show),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (prominent) palette.heroOnContainerColor else colors.primary,
                )
            }
        }
    }
}

@Composable
private fun HomeTicket.Saved.summary(): String {
    val state =
        when (status) {
            Ticket.Status.Refunded -> stringResource(R.string.ticket_status_refunded)
            Ticket.Status.Revoked -> stringResource(R.string.ticket_status_revoked)
            Ticket.Status.Active, Ticket.Status.Unknown -> null
        }
    return listOfNotNull(type.takeIf(String::isNotBlank), number, state).joinToString(stringResource(R.string.detail_separator))
}

@Composable
private fun TicketBadge(
    container: Color,
    content: Color,
) {
    Box(
        modifier =
            Modifier
                .size(48.dp)
                .background(container, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ChaiIcons.ConfirmationNumber,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(24.dp),
        )
    }
}

@ChaiLightAndDarkComposePreviews
@Composable
private fun HomeTicketCardPreview() {
    ChaiTheme {
        Surface {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val saved = HomeTicket.Saved("Magak Emmanuel", "Regular", "TIC-70235", Ticket.Status.Active)
                HomeTicketCard(ticket = HomeTicket.Add, prominent = false, onClick = {})
                HomeTicketCard(ticket = saved, prominent = false, onClick = {})
                HomeTicketCard(ticket = saved, prominent = true, onClick = {})
            }
        }
    }
}