/*
 * Copyright 2023 DroidconKE
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
package com.android254.data.repos.mappers

import com.android254.data.db.model.FeedEntity
import com.android254.data.network.models.responses.FeedDTO
import com.android254.domain.models.Feed

fun FeedDTO.toDomain() = Feed(
    title = title,
    body = body,
    topic = topic,
    url = url,
    image = image,
    createdAt = createdAt.toString()
)

fun FeedDTO.toEntity() = FeedEntity(
    title = title,
    body = body,
    topic = topic,
    url = url,
    image = image,
    createdAt = createdAt.toString()
)

fun FeedEntity.toDomain() = Feed(
    title = title,
    body = body,
    topic = topic,
    url = url,
    image = image,
    createdAt = createdAt
)