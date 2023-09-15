package ke.droidcon.kotlin.datasource.remote.feed

import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.feed.model.FeedDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult

class RemoteFeedDataSourceImpl @Inject constructor(
    private val api: FeedApi
) : RemoteFeedDataSource {

    override suspend fun fetchFeed(): DataResult<List<FeedDTO>> =
        api.fetchFeed()
}