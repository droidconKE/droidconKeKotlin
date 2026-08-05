/*
 * Copyright 2023 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the \"License\");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an \"AS IS\" BASIS,
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
    fun `test sync reconciles data correctly`() = runTest {
        val remoteItems = listOf("item1", "item3")
        val localIds = listOf("item1", "item2")
        val upserter = mockk<suspend (List<String>) -> Unit>(relaxed = true)
        val deleter = mockk<suspend (List<String>) -> Unit>(relaxed = true)

        val result = synchronizer.sync(
            remoteItemFetcher = { remoteItems },
            localIdFetcher = { localIds },
            localItemUpserter = upserter,
            localItemDeleter = deleter,
            remoteToLocalIdSelector = { it }
        )

        assertThat(result.isSuccess, `is`(true))
        coVerify { deleter(listOf("item2")) }
        coVerify { upserter(remoteItems) }
    }

    @Test
    fun `test sync does not delete if no orphans`() = runTest {
        val remoteItems = listOf("item1", "item2")
        val localIds = listOf("item1")
        val upserter = mockk<suspend (List<String>) -> Unit>(relaxed = true)
        val deleter = mockk<suspend (List<String>) -> Unit>(relaxed = true)

        val result = synchronizer.sync(
            remoteItemFetcher = { remoteItems },
            localIdFetcher = { localIds },
            localItemUpserter = upserter,
            localItemDeleter = deleter,
            remoteToLocalIdSelector = { it }
        )

        assertThat(result.isSuccess, `is`(true))
        coVerify(exactly = 0) { deleter(any()) }
        coVerify { upserter(remoteItems) }
    }
}
