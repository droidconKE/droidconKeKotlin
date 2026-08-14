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

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ke.droidcon.kotlin.datasource.local.Database
import ke.droidcon.kotlin.datasource.local.model.SpeakerEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SpeakerDaoTest {
    private lateinit var speakerDao: SpeakerDao
    private lateinit var db: Database

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, Database::class.java)
                .allowMainThreadQueries()
                .build()
        speakerDao = db.speakerDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun `test speakerDao fetches all speakers`() =
        runTest {
            val speaker = createSpeaker(id = 1, name = "Speaker 1")
            speakerDao.insert(speaker)
            val result = speakerDao.fetchSpeakers().first()
            assertThat(result.size, `is`(1))
            assertThat(result[0].name, `is`("Speaker 1"))
        }

    @Test
    fun `test getNames returns all names`() =
        runTest {
            speakerDao.insert(createSpeaker(id = 1, name = "Name 1"))
            speakerDao.insert(createSpeaker(id = 2, name = "Name 2"))

            val result = speakerDao.getNames()
            assertThat(result.size, `is`(2))
            assertThat(result.containsAll(listOf("Name 1", "Name 2")), `is`(true))
        }

    @Test
    fun `test deleteByNames deletes specified speakers`() =
        runTest {
            speakerDao.insert(createSpeaker(id = 1, name = "Name 1"))
            speakerDao.insert(createSpeaker(id = 2, name = "Name 2"))

            speakerDao.deleteByNames(listOf("Name 1"))

            val result = speakerDao.fetchSpeakers().first()
            assertThat(result.size, `is`(1))
            assertThat(result[0].name, `is`("Name 2"))
        }

    private fun createSpeaker(
        id: Int,
        name: String,
    ) = SpeakerEntity(
        id = id,
        name = name,
        tagline = "Tagline",
        bio = "Bio",
        avatar = "Avatar",
        twitter = "Twitter $id",
    )
}