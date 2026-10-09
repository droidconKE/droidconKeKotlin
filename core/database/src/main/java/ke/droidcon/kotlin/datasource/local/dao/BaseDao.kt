/*
 * Copyright 2022 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ke.droidcon.kotlin.datasource.local.dao

import androidx.room.Delete
import androidx.room.Upsert

/**
 * BaseDao<T>
 *
 * This dao interface makes it easy to abstract commonly used room operations
 *
 * @param T takes in the data class
 */
interface BaseDao<T> {
    @Upsert
    suspend fun upsert(item: T)

    @Upsert
    suspend fun upsert(vararg items: T)

    @Upsert
    suspend fun upsert(items: List<T>)

    @Delete
    suspend fun delete(item: T)
}