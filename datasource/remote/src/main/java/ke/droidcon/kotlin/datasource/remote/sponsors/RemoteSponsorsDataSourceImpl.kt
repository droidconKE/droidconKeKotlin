package ke.droidcon.kotlin.datasource.remote.sponsors

import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.sponsors.model.SponsorDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RemoteSponsorsDataSourceImpl(
    private val api: SponsorsApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RemoteSponsorsDataSource {

    override suspend fun getAllSponsorsRemote(): DataResult<List<SponsorDTO>> {
        return withContext(ioDispatcher) {
            when (val response = api.fetchSponsors()) {
                is DataResult.Success -> {
                    val sponsors = response.data.data
                    return@withContext DataResult.Success(data = sponsors)
                }
                is DataResult.Error -> {
                    return@withContext DataResult.Error(message = response.message)
                }
                is DataResult.Loading -> {
                    return@withContext DataResult.Loading(emptyList())
                }
                is DataResult.Empty -> {
                    return@withContext DataResult.Error(message = "Sponsors Information not found")
                }
            }
        }
    }
}