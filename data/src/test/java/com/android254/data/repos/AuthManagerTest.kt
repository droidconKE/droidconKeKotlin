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
package com.android254.data.repos

import com.android254.domain.models.DataResult
import com.android254.domain.models.Success
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import ke.droidcon.kotlin.datasource.remote.auth.AuthApi
import ke.droidcon.kotlin.datasource.remote.auth.model.AccessTokenDTO
import ke.droidcon.kotlin.datasource.remote.auth.model.UserDetailsDTO
import ke.droidcon.kotlin.datasource.remote.utils.NetworkError
import ke.droidcon.kotlin.datasource.remote.utils.TokenProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class AuthManagerTest {
    private val mockApi = mockk<AuthApi>()
    private val mockTokenProvider = mockk<TokenProvider>()
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val fakeUserDetails = UserDetailsDTO(
        name = "Test",
        email = "test@gmail.com",
        gender = null,
        avatar = "http://test.com"
    )

    @Test
    fun `test getAndSaveApiToken successfully`() {
        runBlocking {
            val repo = AuthManager(mockApi, mockTokenProvider, ioDispatcher)
            coEvery { mockApi.googleLogin(any()) } returns AccessTokenDTO(
                "test",
                user = fakeUserDetails
            )
            coEvery { mockTokenProvider.update(any()) } just Runs

            val result = repo.getAndSaveApiToken("test")
            assertThat(result, `is`(DataResult.Success(Success)))
            coVerify { mockTokenProvider.update("test") }
        }
    }

    @Test
    fun `test getAndSaveApiToken failure - network error`() {
        runBlocking {
            val repo = AuthManager(mockApi, mockTokenProvider, ioDispatcher)
            val exc = NetworkError()

            coEvery { mockApi.googleLogin(any()) } throws exc
            val result = repo.getAndSaveApiToken("test")
            assertThat(result, `is`(DataResult.Error("Login failed", true, exc)))
        }
    }

    @Test
    fun `test getAndSaveApiToken failure - other error`() {
        runBlocking {
            val repo = AuthManager(mockApi, mockTokenProvider, ioDispatcher)
            val exc = Exception()

            coEvery { mockApi.googleLogin(any()) } throws exc
            val result = repo.getAndSaveApiToken("test")
            assertThat(result, `is`(DataResult.Error("Login failed", exc = exc)))
        }
    }
}