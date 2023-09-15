package ke.droidcon.kotlin.datasource.remote.sessions

import android.os.Build
import androidx.annotation.RequiresApi
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.sessions.model.SessionDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RemoteSessionsDataSourceImpl(
    private val api: SessionsApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RemoteSessionsDataSource {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getAllSessionsRemote(): DataResult<List<SessionDTO>> {
        return withContext(ioDispatcher) {
            val response = api.fetchSessions()
            val sessions = response.data.flatMap { (_, value) -> value }
            return@withContext DataResult.Success(data = sessions)
        }
    }
}