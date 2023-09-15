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
package com.android254.data.repos.mappers

import com.android254.data.db.model.OrganizerEntity
import com.android254.domain.models.Organizer
import ke.droidcon.kotlin.datasource.remote.organizers.model.OrganizerDTO

fun OrganizerDTO.toEntity() = OrganizerEntity(
    id = 0,
    name = name.orEmpty(),
    tagline = tagline.orEmpty(),
    link = link.orEmpty(),
    type = type.orEmpty(),
    photo = photo.orEmpty(),
    createdAt = createdAt.orEmpty(),
    designation = designation.orEmpty(),
    bio = bio.orEmpty(),
    twitterHandle = twitterHandle.orEmpty()
)

fun OrganizerEntity.toDomain() = Organizer(
    id = id,
    name = name,
    tagline = tagline,
    link = link,
    type = type,
    photo = photo,
    createdAt = createdAt,
    bio = bio,
    twitterHandle = twitterHandle,
    designation = designation
)