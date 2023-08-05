package com.android254.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.android254.data.db.model.SponsorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SponsorsDao : BaseDao<SponsorEntity> {

    @Query("SELECT * FROM sponsors")
    fun fetchCachedSponsors(): Flow<List<SponsorEntity>>

    @Query("DELETE FROM sponsors")
    suspend fun deleteAllCachedSponsors()
}