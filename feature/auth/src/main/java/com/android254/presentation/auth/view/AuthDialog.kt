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
package com.android254.presentation.auth.view

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.auth.AuthViewModel
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import ke.droidcon.kotlin.core.ui.R
import kotlinx.coroutines.launch

@Composable
fun AuthDialog(
    onDismiss: () -> Unit = {},
    viewModel: (() -> AuthViewModel)? = null,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val signInFailedMessage = stringResource(id = R.string.google_sign_in_failed)
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(id = R.string.auth_dialog_title),
                style = MaterialTheme.typography.headlineSmallEmphasized,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    text = stringResource(id = R.string.auth_dialog_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                GoogleSignInButton(
                    text = stringResource(id = R.string.sign_in_with_google_label),
                    icon = painterResource(id = R.drawable.btn_google_icon),
                    isLoading = loading,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("google_button"),
                    onClick = {
                        val authViewModel = viewModel?.invoke()
                        if (authViewModel != null && activity != null) {
                            loading = true
                            coroutineScope.launch {
                                val signedIn = authViewModel.signIn(activity)
                                loading = false
                                if (signedIn) {
                                    onDismiss()
                                } else {
                                    Toast
                                        .makeText(context, signInFailedMessage, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_button")) {
                Text(text = stringResource(id = R.string.auth_dialog_dismiss))
            }
        },
    )
}

@ChaiLightAndDarkComposePreviews
@Composable
private fun AuthDialogPreview() {
    ChaiTheme {
        AuthDialog()
    }
}