package com.android254.data.repos.remote

import com.android254.data.di.IoDispatcher
import com.android254.data.network.apis.SponsorsApi
import com.android254.data.network.models.responses.SponsorDTO
import com.android254.domain.models.DataResult
import com.android254.domain.models.ResourceResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext


interface RemoteSponsorsDataSource {

    suspend fun getAllSponsorsRemote(): ResourceResult<List<SponsorDTO>>
}
class RemoteSponsorsDataSourceImpl (
    private val api:SponsorsApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
):RemoteSponsorsDataSource{

    override suspend fun getAllSponsorsRemote(): ResourceResult<List<SponsorDTO>> {
        return withContext(ioDispatcher){
            val response = api.fetchSponsors()
            when(response){
                is DataResult.Success -> {
                    val sponsors = response.data.data
                    return@withContext ResourceResult.Success(data = sponsors)
                }
                is DataResult.Error -> {
                    return@withContext ResourceResult.Error(message = response.message)
                }
                is DataResult.Loading -> {
                    return@withContext ResourceResult.Loading()
                }
                is DataResult.Empty -> {
                    return@withContext ResourceResult.Error(message = "Sponsors Information not found")
                }
            }
        }

    }

}