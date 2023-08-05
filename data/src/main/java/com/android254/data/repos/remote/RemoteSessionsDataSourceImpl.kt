package com.android254.data.repos.remote

import android.os.Build
import androidx.annotation.RequiresApi
import com.android254.data.di.IoDispatcher
import com.android254.data.network.apis.SessionsApi
import com.android254.data.network.models.responses.SessionDTO
import com.android254.domain.models.ResourceResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface RemoteSessionsDataSource {

    suspend fun getAllSessionsRemote(): ResourceResult<List<SessionDTO>>
}

class RemoteSessionsDataSourceImpl (
    private val api: SessionsApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
):RemoteSessionsDataSource {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getAllSessionsRemote(): ResourceResult<List<SessionDTO>> {
        return withContext(ioDispatcher){
            val response = api.fetchSessions()
            val sessions = response.data.flatMap { (_, value) -> value }
            return@withContext ResourceResult.Success(data = sessions)
        }
    }




}