package com.android254.shared.domain.repos

import com.android254.shared.domain.models.HomeDetails
import kotlinx.coroutines.flow.Flow

interface HomeRepo {
    fun fetchHomeDetails(): Flow<HomeDetails>
}