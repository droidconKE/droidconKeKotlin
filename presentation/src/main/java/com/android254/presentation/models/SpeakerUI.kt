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
package com.android254.presentation.models

data class SpeakerUI(
    val id: Int = 0,
    val imageUrl: String? = "",
    val name: String = "Name",
    val tagline: String? = "",
    val bio: String? = "bio",
    val twitterHandle: String? = "TwitterHandle"
)

val speakersDummyData = listOf(
    SpeakerUI(
        imageUrl = "https://sessionize.com/image/09c1-400o400o2-cf-9587-423b-bd2e-415e6757286c.b33d8d6e-1f94-4765-a797-255efc34390d.jpg",
        name = "Harun Wangereka",
        bio = "Kenya Partner Lead at droidcon Berlin | Android | Kotlin | Flutter | C++"
    ),
    SpeakerUI(
        imageUrl = "https://media-exp1.licdn.com/dms/image/C4D03AQGn58utIO-x3w/profile-displayphoto-shrink_200_200/0/1637478114039?e=2147483647&v=beta&t=3kIon0YJQNHZojD3Dt5HVODJqHsKdf2YKP1SfWeROnI",
        name = "Frank Tamre",
        bio = "Kenya Partner Lead at droidcon Berlin | Android | Kotlin | Flutter | C++"
    )
)