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
package com.android254.presentation.feed.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.domain.models.ScheduleUpdate.Kind
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R
import ke.droidcon.kotlin.chai.R as ChaiR

@Composable
internal fun ScheduleUpdateRow(
    item: UpdateItem.Schedule,
    onSessionClick: (String) -> Unit,
    onBrowseSessions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sessionId = item.sessionId
    val onClick: (() -> Unit)? =
        when {
            sessionId != null -> ({ onSessionClick(sessionId) })
            item.kind == Kind.ScheduleOut || item.kind == Kind.ManyChanges -> onBrowseSessions
            else -> null
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KindTile(kind = item.kind, isStarred = item.isStarred)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.kind.label().uppercase(),
                    style = MaterialTheme.typography.labelSmallEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = item.whenLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = item.title.ifBlank { item.kind.headline() },
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
            item.detail()?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (item.isStarred) {
                Text(
                    text = stringResource(R.string.rail_your_session),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun KindTile(
    kind: Kind,
    isStarred: Boolean,
) {
    val container = if (isStarred) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
    val content = if (isStarred) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier = Modifier.size(40.dp).background(container, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = kind.icon(), contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Kind.icon(): Painter =
    when (this) {
        Kind.Added -> rememberVectorPainter(ChaiIcons.CoPresent)
        Kind.Removed -> rememberVectorPainter(ChaiIcons.Clear)
        Kind.ScheduleOut, Kind.Rescheduled, Kind.Moved, Kind.ManyChanges -> painterResource(ChaiR.drawable.sessions_icon)
    }

@Composable
private fun Kind.label(): String =
    stringResource(
        when (this) {
            Kind.ScheduleOut, Kind.ManyChanges -> R.string.update_schedule_label
            Kind.Added -> R.string.update_added_label
            Kind.Rescheduled -> R.string.update_rescheduled_label
            Kind.Moved -> R.string.update_moved_label
            Kind.Removed -> R.string.update_removed_label
        },
    )

@Composable
private fun Kind.headline(): String = stringResource(if (this == Kind.ScheduleOut) R.string.update_schedule_out_headline else R.string.update_many_headline)

@Composable
private fun UpdateItem.Schedule.detail(): String? {
    val time = sessionTime
    return when (kind) {
        Kind.ScheduleOut -> pluralStringResource(R.plurals.update_schedule_out_detail, count, count)
        Kind.ManyChanges -> pluralStringResource(R.plurals.update_many_detail, count, count)
        Kind.Added -> listOfNotNull(time, venue.takeIf(String::isNotBlank)).joinToString(" · ").ifBlank { null }
        Kind.Rescheduled -> time?.let { stringResource(R.string.update_now, it) }
        Kind.Moved -> venue.takeIf(String::isNotBlank)?.let { stringResource(R.string.update_now_in, it) }
        Kind.Removed -> stringResource(R.string.update_removed_detail)
    }
}