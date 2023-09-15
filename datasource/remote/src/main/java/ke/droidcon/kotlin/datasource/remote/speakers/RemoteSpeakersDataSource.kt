package ke.droidcon.kotlin.datasource.remote.speakers

import ke.droidcon.kotlin.datasource.remote.speakers.model.SpeakerDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult

interface RemoteSpeakersDataSource {

    suspend fun getAllSpeakersRemote(): DataResult<List<SpeakerDTO>>
}