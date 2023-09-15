package ke.droidcon.kotlin.datasource.remote.sponsors

import ke.droidcon.kotlin.datasource.remote.sponsors.model.SponsorDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult

interface RemoteSponsorsDataSource {

    suspend fun getAllSponsorsRemote(): DataResult<List<SponsorDTO>>
}