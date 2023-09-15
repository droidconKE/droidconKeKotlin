package ke.droidcon.kotlin.datasource.remote.sessions

import ke.droidcon.kotlin.datasource.remote.sessions.model.SessionDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult

interface RemoteSessionsDataSource {

    suspend fun getAllSessionsRemote(): DataResult<List<SessionDTO>>
}
