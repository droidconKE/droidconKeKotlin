package ke.droidcon.kotlin.datasource.remote.speakers

import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.speakers.model.SpeakerDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RemoteSpeakersDataSourceImpl @Inject constructor(
    private val api: SpeakersApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RemoteSpeakersDataSource {

    override suspend fun getAllSpeakersRemote(): DataResult<List<SpeakerDTO>> {
        return withContext(ioDispatcher) {
            when (val response = api.fetchSpeakers()) {
                is DataResult.Success -> {
                    val speakers = response.data.data
                    return@withContext DataResult.Success(data = speakers)
                }
                is DataResult.Error -> {
                    return@withContext DataResult.Error(message = response.message)
                }
                is DataResult.Loading -> {
                    return@withContext DataResult.Loading(emptyList())
                }
                is DataResult.Empty -> {
                    return@withContext DataResult.Error(message = "Speakers Information not found")
                }
            }
        }
    }
}