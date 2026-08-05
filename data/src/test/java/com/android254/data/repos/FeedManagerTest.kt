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
package com.android254.data.repos

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import ke.droidcon.kotlin.datasource.local.source.LocalFeedDataSource
import ke.droidcon.kotlin.datasource.remote.feed.RemoteFeedDataSource
import ke.droidcon.kotlin.datasource.remote.feed.model.FeedDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class FeedManagerTest {
    private val mockLocalFeedDataSource = mockk<LocalFeedDataSource>()
    private val mockRemoteFeedDataSource = mockk<RemoteFeedDataSource>()

    @Test
    fun `test syncWith reconciles data`() =
        runTest {
            val remoteFeed = mockk<FeedDTO>()
            every { remoteFeed.title } returns "Feed 1"
            coEvery { mockRemoteFeedDataSource.fetchFeed() } returns DataResult.Success(listOf(remoteFeed))
            coEvery { mockLocalFeedDataSource.getTitles() } returns listOf("Feed 1", "Feed 2")
            coEvery { mockLocalFeedDataSource.insertFeed(any()) } returns Unit
            coEvery { mockLocalFeedDataSource.deleteByTitles(any()) } returns Unit

            val manager = FeedManager(mockLocalFeedDataSource, mockRemoteFeedDataSource)
            val result = manager.syncWith(mockk())

            assert(result)
            coVerify { mockLocalFeedDataSource.deleteByTitles(listOf("Feed 2")) }
            coVerify { mockLocalFeedDataSource.insertFeed(any()) }
        }
}
