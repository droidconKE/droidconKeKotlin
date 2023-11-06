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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiTextButtonLight
import ke.droidcon.kotlin.presentation.R
import kotlinx.coroutines.launch

@Composable
fun AuthDialog(
    onDismiss: () -> Unit = {},
    viewModel: (() -> AuthViewModel)? = null
) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val googleSignInLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            coroutineScope.launch {
                if (viewModel?.invoke()?.submitGoogleToken(result.data) == true) {
                    loading = false
                    onDismiss()
                } else {
                    loading = false
                    Toast.makeText(context, "Google sign in failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    AlertDialog(
        modifier = Modifier,
        containerColor = MaterialTheme.chaiColorsPalette.surfaces,
        shape = RoundedCornerShape(12.dp),
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(color = MaterialTheme.chaiColorsPalette.surfaces)
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                )
                GoogleSignInButton(
                    text = stringResource(id = R.string.sign_in_with_google_label),
                    icon = painterResource(id = R.drawable.btn_google_icon),
                    borderColor = ChaiWhite,
                    backgroundColor = ChaiWhite,
                    isLoading = loading,
                    modifier = Modifier
                        .width(288.dp)
                        .testTag("google_button"),
                    onClick = {
                        viewModel?.invoke()?.let {
                            loading = true
                            googleSignInLauncher.launch(it.getSignInIntent())
                        }
                    }
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                ChaiTextButtonLight(
                    modifier = Modifier.testTag("cancel_button"),
                    bodyText = "Cancel", // Strangely, using stringResource(..) causes a build error,
                    textColor = MaterialTheme.chaiColorsPalette.textWeakColor
                )
            }
        }
    )
}

@ChaiLightAndDarkComposePreview
@Composable
fun AuthDialogPreview() {
    ChaiDCKE22Theme() {
        AuthDialog()
    }
}