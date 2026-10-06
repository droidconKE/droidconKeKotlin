/*
 * Copyright 2023 DroidconKE
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
package com.android254.presentation.sessions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.components.ChaiTextLabelSmall
import ke.droidcon.kotlin.core.ui.R

@Composable
fun MySessionsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateText = stringResource(if (checked) R.string.my_sessions_on else R.string.my_sessions_off)
    Column(
        modifier =
            modifier
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                .semantics { stateDescription = stateText },
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            thumbContent = {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            },
        )
        ChaiTextLabelSmall(
            bodyText = stringResource(R.string.my_sessions),
            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@ChaiLightAndDarkComposePreviews
@Composable
private fun MySessionsSwitchPreview() {
    ChaiTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MySessionsSwitch(checked = false, onCheckedChange = {})
            MySessionsSwitch(checked = true, onCheckedChange = {})
        }
    }
}