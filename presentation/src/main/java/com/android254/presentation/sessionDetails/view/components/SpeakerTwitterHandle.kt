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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.COutlinedButton
import com.droidconke.chai.components.ChaiBodyMedium
import ke.droidcon.kotlin.presentation.R

@Composable
fun SpeakerTwitterHandle(
    speaker: SessionDetailsSpeakerPresentationModel,
) {
    val context = LocalContext.current
    val intent =
        remember {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.twitter.com/${speaker.twitterHandle}"),
            )
        }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ChaiBodyMedium(
            bodyText = stringResource(R.string.twitter_handle_label),
            textColor = MaterialTheme.chaiColorsPalette.textNormalColor,
        )

        COutlinedButton(
            onClick = { context.startActivity(intent) },
            shape = RoundedCornerShape(10.dp),
            colors =
                ButtonDefaults.buttonColors(
                    MaterialTheme.chaiColorsPalette.outlinedButtonBackgroundColor,
                ),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_twitter_logo),
                contentDescription = null,
                modifier =
                    Modifier
                        .height(20.dp)
                        .width(20.dp),
                tint = MaterialTheme.chaiColorsPalette.secondaryButtonColor,
            )

            Spacer(modifier = Modifier.width(5.dp))

            ChaiBodyMedium(
                modifier = Modifier.testTag(TestTag.TWITTER_HANDLE_TEXT),
                bodyText = speaker.twitterHandle,
                textColor = MaterialTheme.chaiColorsPalette.secondaryButtonColor,
            )
        }
    }
}