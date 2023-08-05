package com.android254.data.repos.local

import com.android254.data.dao.SpeakerDao
import com.android254.data.di.IoDispatcher
import com.android254.data.network.models.responses.SpeakerDTO
import com.android254.data.repos.mappers.toDomainModel
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.Speaker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject


interface LocalSpeakersDataSource {

    fun getCachedSpeakers(): Flow<List<Speaker>>

    suspend fun getCachedSpeakerById(speakerId:Int):Speaker?

    fun fetchCachedSpeakerCount():Flow<Int>

    suspend fun deleteAllCachedSpeakers()

    suspend fun saveCachedSpeakers(speakers:List<SpeakerDTO>)

}
class LocalSpeakersDataSourceImpl  @Inject constructor(
    private val speakerDao: SpeakerDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
):LocalSpeakersDataSource {

    override fun getCachedSpeakers(): Flow<List<Speaker>> {
        return speakerDao.fetchSpeakers()
            .map { speakers ->  speakers.map { it.toDomainModel() } }
            .flowOn(ioDispatcher)


    }

    override suspend fun saveCachedSpeakers(speakers: List<SpeakerDTO>) {
        return speakerDao.insert(items = speakers.map { it.toEntity() })
    }

    override suspend fun getCachedSpeakerById(speakerId: Int): Speaker? {
        return withContext(ioDispatcher){
            speakerDao.getSpeakerById(id = speakerId)?.toDomainModel()
        }

    }

    override fun fetchCachedSpeakerCount(): Flow<Int> {
        return speakerDao.fetchSpeakerCount().flowOn(ioDispatcher)
    }

    override suspend fun deleteAllCachedSpeakers() {
        withContext(ioDispatcher){
            speakerDao.deleteAll()
        }
    }


}