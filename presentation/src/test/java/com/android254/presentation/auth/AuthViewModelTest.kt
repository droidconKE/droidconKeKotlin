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
package com.android254.presentation.auth

import android.app.Activity
import com.android254.domain.models.DataResult
import com.android254.domain.models.Success
import com.android254.domain.repos.AuthRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthViewModelTest {
    private val googleSignInHandler = mockk<GoogleSignInHandler>()
    private val authRepo = mockk<AuthRepo>()
    private val activity = mockk<Activity>()

    private val viewModel = AuthViewModel(googleSignInHandler, authRepo)

    @Test
    fun `exchanges a google id token for an api token`() =
        runTest {
            coEvery { googleSignInHandler.signIn(activity) } returns Result.success("id-token")
            coEvery { authRepo.getAndSaveApiToken("id-token") } returns DataResult.Success(Success)

            assertTrue(viewModel.signIn(activity))
        }

    /** A dismissed sheet must not reach the token endpoint. */
    @Test
    fun `does not call the api when the credential request fails`() =
        runTest {
            coEvery { googleSignInHandler.signIn(activity) } returns
                Result.failure(IllegalStateException("cancelled"))

            assertFalse(viewModel.signIn(activity))

            coVerify(exactly = 0) { authRepo.getAndSaveApiToken(any()) }
        }

    @Test
    fun `reports failure when the token exchange fails`() =
        runTest {
            coEvery { googleSignInHandler.signIn(activity) } returns Result.success("id-token")
            coEvery { authRepo.getAndSaveApiToken("id-token") } returns
                DataResult.Error(message = "boom")

            assertFalse(viewModel.signIn(activity))
        }
}