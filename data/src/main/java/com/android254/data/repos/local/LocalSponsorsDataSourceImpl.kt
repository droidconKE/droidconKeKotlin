package com.android254.data.repos.local

import com.android254.data.dao.SponsorsDao
import com.android254.data.di.IoDispatcher
import com.android254.data.network.models.responses.SponsorDTO
import com.android254.data.repos.mappers.toDomain
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.Sponsors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject


interface LocalSponsorsDataSource {

    fun fetchCachedSponsors(): Flow<List<Sponsors>>

    suspend fun deleteCachedSponsors()

    suspend fun saveCachedSponsors(sponsors:List<SponsorDTO>)
}
class LocalSponsorsDataSourceImpl @Inject constructor(
    private val sponsorsDao: SponsorsDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
):LocalSponsorsDataSource {

    override fun fetchCachedSponsors(): Flow<List<Sponsors>> {
        return sponsorsDao.fetchCachedSponsors()
            .flowOn(ioDispatcher)
            .map { it.map { it.toDomain() }

        }
    }

    override suspend fun deleteCachedSponsors() {
        withContext(ioDispatcher){
            sponsorsDao.deleteAllCachedSponsors()
        }
    }

    override suspend fun saveCachedSponsors(sponsors: List<SponsorDTO>) {
        withContext(ioDispatcher){
            sponsorsDao.insert(items = sponsors.map { it.toEntity() })
        }
    }
}