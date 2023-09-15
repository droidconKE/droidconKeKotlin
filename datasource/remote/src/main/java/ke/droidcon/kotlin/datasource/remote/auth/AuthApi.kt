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
package ke.droidcon.kotlin.datasource.remote.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.auth.model.AccessTokenDTO
import ke.droidcon.kotlin.datasource.remote.auth.model.GoogleToken
import ke.droidcon.kotlin.datasource.remote.auth.model.StatusDTO
import ke.droidcon.kotlin.datasource.remote.utils.provideBaseUrl
import ke.droidcon.kotlin.datasource.remote.utils.safeApiCall

class AuthApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun googleLogin(token: GoogleToken): AccessTokenDTO = safeApiCall {
        return@safeApiCall client.post("${provideBaseUrl()}/social_login/google") {
            setBody(token)
        }.body()
    }

    suspend fun logout(): StatusDTO = safeApiCall {
        val url = "${provideBaseUrl()}/logout"
        return@safeApiCall client.post(url).body()
    }
}