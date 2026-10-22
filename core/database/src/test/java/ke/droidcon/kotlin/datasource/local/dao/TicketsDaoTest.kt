/*
 * Copyright 2026 DroidconKE
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
import ke.droidcon.kotlin.datasource.local.model.TicketEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TicketsDaoTest {
    private lateinit var db: Database
    private lateinit var ticketsDao: TicketsDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), Database::class.java).allowMainThreadQueries().build()
        ticketsDao = db.ticketsDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `replacing keeps one ticket on the phone`() =
        runTest {
            ticketsDao.replace(ticket("TIC-1"))
            ticketsDao.replace(ticket("TIC-2"))

            assertEquals("TIC-2", ticketsDao.ticket().first()?.number)
            assertEquals("TIC-2", ticketsDao.current()?.number)
        }

    @Test
    fun `updating a ticket that was removed does not bring it back`() =
        runTest {
            ticketsDao.replace(ticket("TIC-1"))
            ticketsDao.clear()

            ticketsDao.update(ticket("TIC-1", status = "refunded"))

            assertNull(ticketsDao.current())
        }

    private fun ticket(
        number: String,
        status: String = "active",
    ) = TicketEntity(
        number = number,
        attendeeName = "Ada Lovelace",
        attendeeEmail = "ada@example.com",
        type = "Early bird",
        event = "droidconKE 2026",
        firstDay = "2026-11-05",
        lastDay = "2026-11-06",
        venue = "PrideInn Azure",
        venueAddress = "Westlands, Nairobi",
        status = status,
        checkIns = "[]",
        qrBase64 = "",
    )
}