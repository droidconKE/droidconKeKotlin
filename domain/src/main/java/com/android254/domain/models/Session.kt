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
package com.android254.domain.models

data class Session(
    val id: String,
    val endDateTime: String,
    val endTime: String,
    val isBookmarked: Boolean,
    val isKeynote: Boolean,
    val isServiceSession: Boolean,
    val sessionImage: String?,
    val startDateTime: String,
    val startTime: String,
    val rooms: String,
    val speakers: String,
    val remote_id: String,
    val description: String,
    val sessionFormat: String,
    val sessionLevel: String,
    val slug: String,
    val title: String
)