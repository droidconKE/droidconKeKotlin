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
package com.android254.presentation.sessionDetails.view.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.adaptive.clickablePointer
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.droidconke.chai.components.ChaiBodyMedium
import ke.droidcon.kotlin.core.ui.R
import timber.log.Timber

@Composable
fun SpeakerTwitterHandle(
    speaker: SessionDetailsSpeakerPresentationModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ChaiBodyMedium(
            bodyText = stringResource(R.string.twitter_handle_label),
            textColor = MaterialTheme.colorScheme.onSurface,
        )

        Button(
            modifier = Modifier.clickablePointer(),
            onClick = {
                val uri = twitterProfileUri(speaker.twitterHandle) ?: return@Button
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (e: ActivityNotFoundException) {
                    Timber.w(e, "No activity available to open speaker profile")
                }
            },
            shape = CircleShape,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_twitter_logo),
                contentDescription = null,
                modifier =
                    Modifier
                        .height(20.dp)
                        .width(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(5.dp))

            ChaiBodyMedium(
                modifier = Modifier.testTag(TestTag.TWITTER_HANDLE_TEXT),
                bodyText = speaker.twitterHandle,
                textColor = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

internal fun twitterProfileUri(handle: String): Uri? {
    val cleaned = handle.trim().removePrefix("@")
    if (cleaned.isEmpty()) return null
    return Uri
        .Builder()
        .scheme("https")
        .authority("www.twitter.com")
        .appendPath(cleaned)
        .build()
}