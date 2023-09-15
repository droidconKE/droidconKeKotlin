package ke.droidcon.kotlin.datasource.remote.feed

import ke.droidcon.kotlin.datasource.remote.feed.model.FeedDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult

interface RemoteFeedDataSource {

    suspend fun fetchFeed(): DataResult<List<FeedDTO>>
}