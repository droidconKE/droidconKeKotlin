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
package com.android254.data.util

import com.android254.domain.sync.Synchronizer
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class SyncUtilitiesTest {
    private val synchronizer = object : Synchronizer {}

    @Test
    fun `test sync reconciles data correctly`() =
        runTest {
            val remoteItems = listOf("item1", "item3")
            val localIds = listOf("item1", "item2")
            val upserter = mockk<suspend (List<String>) -> Unit>(relaxed = true)
            val deleter = mockk<suspend (List<String>) -> Unit>(relaxed = true)

            val result =
                synchronizer.sync(
                    remoteItemFetcher = { remoteItems },
                    localIdFetcher = { localIds },
                    localItemUpserter = upserter,
                    localItemDeleter = deleter,
                    remoteToLocalIdSelector = { it },
                )

            assertThat(result.isSuccess, `is`(true))
            coVerify { deleter(listOf("item2")) }
            coVerify { upserter(remoteItems) }
        }

    @Test
    fun `test sync does not delete if no orphans`() =
        runTest {
            val remoteItems = listOf("item1", "item2")
            val localIds = listOf("item1")
            val upserter = mockk<suspend (List<String>) -> Unit>(relaxed = true)
            val deleter = mockk<suspend (List<String>) -> Unit>(relaxed = true)

            val result =
                synchronizer.sync(
                    remoteItemFetcher = { remoteItems },
                    localIdFetcher = { localIds },
                    localItemUpserter = upserter,
                    localItemDeleter = deleter,
                    remoteToLocalIdSelector = { it },
                )

            assertThat(result.isSuccess, `is`(true))
            coVerify(exactly = 0) { deleter(any()) }
            coVerify { upserter(remoteItems) }
        }

    @Test
    fun `an empty remote list leaves local data alone when asked to`() =
        runTest {
            val upserter = mockk<suspend (List<String>) -> Unit>(relaxed = true)
            val deleter = mockk<suspend (List<String>) -> Unit>(relaxed = true)

            val result =
                synchronizer.sync(
                    remoteItemFetcher = { emptyList() },
                    localIdFetcher = { listOf("item1") },
                    localItemUpserter = upserter,
                    localItemDeleter = deleter,
                    remoteToLocalIdSelector = { it },
                    keepLocalWhenRemoteIsEmpty = true,
                )

            assertThat(result.isSuccess, `is`(true))
            coVerify(exactly = 0) { deleter(any()) }
            coVerify(exactly = 0) { upserter(any()) }
        }

    @Test
    fun `an empty remote list deletes local data by default`() =
        runTest {
            val deleter = mockk<suspend (List<String>) -> Unit>(relaxed = true)

            synchronizer.sync(
                remoteItemFetcher = { emptyList<String>() },
                localIdFetcher = { listOf("item1") },
                localItemUpserter = {},
                localItemDeleter = deleter,
                remoteToLocalIdSelector = { it },
            )

            coVerify { deleter(listOf("item1")) }
        }

    @Test
    fun `local writes run inside one transaction and the fetch runs outside it`() =
        runTest {
            val events = mutableListOf<String>()
            val transactional =
                object : Synchronizer {
                    override suspend fun <R> inTransaction(block: suspend () -> R): R {
                        events += "begin"
                        return block().also { events += "commit" }
                    }
                }

            transactional.sync(
                remoteItemFetcher = { listOf("item1").also { events += "fetch" } },
                localIdFetcher = { listOf("item2").also { events += "read" } },
                localItemUpserter = { events += "upsert" },
                localItemDeleter = { events += "delete" },
                remoteToLocalIdSelector = { it },
            )

            assertThat(events, `is`(listOf("fetch", "begin", "read", "delete", "upsert", "commit")))
        }
}