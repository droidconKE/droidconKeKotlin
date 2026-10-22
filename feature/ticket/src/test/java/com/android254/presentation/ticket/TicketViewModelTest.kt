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
package com.android254.presentation.ticket

import androidx.lifecycle.SavedStateHandle
import com.android254.domain.models.Ticket
import com.android254.domain.repos.AddTicketResult
import ke.droidcon.kotlin.core.testing.FakeTicketsRepo
import ke.droidcon.kotlin.core.testing.testTicket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TicketViewModelTest {
    private val nairobi = TimeZone.of("Africa/Nairobi")
    private val dayOne =
        object : Clock {
            override fun now() = Instant.parse("2026-11-05T10:00:00+03:00")
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `opening the ticket refreshes it once`() =
        runTest {
            val repo = FakeTicketsRepo(testTicket())

            TicketViewModel(repo, dayOne, nairobi, SavedStateHandle())

            assertEquals(1, repo.refreshes)
        }

    @Test
    fun `a ticket checked in today says so, one from yesterday does not`() =
        runTest {
            val today = testTicket(checkIns = listOf(Ticket.CheckIn(LocalDate(2026, 11, 5), Instant.parse("2026-11-05T08:41:00+03:00"))))
            val yesterday = testTicket(checkIns = listOf(Ticket.CheckIn(LocalDate(2026, 11, 4), Instant.parse("2026-11-04T08:41:00+03:00"))))

            assertEquals(true, ready(TicketViewModel(FakeTicketsRepo(today), dayOne, nairobi, SavedStateHandle())).ticket?.checkedInToday)
            assertEquals(false, ready(TicketViewModel(FakeTicketsRepo(yesterday), dayOne, nairobi, SavedStateHandle())).ticket?.checkedInToday)
        }

    @Test
    fun `adding needs a number and an email, then clears the form`() =
        runTest {
            val repo = FakeTicketsRepo()
            val viewModel = TicketViewModel(repo, dayOne, nairobi, SavedStateHandle())
            collect(viewModel)

            viewModel.onAction(TicketAction.NumberChanged("tic-70235"))
            viewModel.onAction(TicketAction.Add)
            assertNull(repo.lastLookUp)

            viewModel.onAction(TicketAction.EmailChanged("magak@example.com"))
            viewModel.onAction(TicketAction.Add)

            assertEquals("tic-70235" to "magak@example.com", repo.lastLookUp)
            assertEquals(AddTicketForm(), ready(viewModel).form)
            assertEquals("tic-70235", ready(viewModel).ticket?.number)
        }

    @Test
    fun `what was typed survives the app being killed in the background`() =
        runTest {
            val saved = SavedStateHandle()
            TicketViewModel(FakeTicketsRepo(), dayOne, nairobi, saved).apply {
                onAction(TicketAction.NumberChanged("TIC-70235"))
                onAction(TicketAction.EmailChanged("magak@example.com"))
            }

            val restored = TicketViewModel(FakeTicketsRepo(), dayOne, nairobi, SavedStateHandle(saved.keys().associateWith { saved.get<String>(it) }))

            assertEquals(AddTicketForm(number = "TIC-70235", email = "magak@example.com"), ready(restored).form)
        }

    @Test
    fun `a failed lookup keeps what was typed and says why`() =
        runTest {
            val viewModel = TicketViewModel(FakeTicketsRepo(nextResult = AddTicketResult.Busy), dayOne, nairobi, SavedStateHandle())
            collect(viewModel)

            viewModel.onAction(TicketAction.NumberChanged("TIC-1"))
            viewModel.onAction(TicketAction.EmailChanged("a@b.c"))
            viewModel.onAction(TicketAction.Add)

            val form = ready(viewModel).form
            assertEquals(AddTicketError.Busy, form.error)
            assertEquals("TIC-1", form.number)
            assertFalse(form.isAdding)
        }

    @Test
    fun `a scanned ticket needs no typing`() =
        runTest {
            val viewModel = TicketViewModel(FakeTicketsRepo(), dayOne, nairobi, SavedStateHandle())
            collect(viewModel)

            viewModel.onScanned("https://events.droidcon.co.ke/checkin/droidconke-2026-731/attendee/TIC-2")

            assertEquals("https://events.droidcon.co.ke/checkin/droidconke-2026-731/attendee/TIC-2", ready(viewModel).ticket?.number)
        }

    @Test
    fun `removing takes the ticket off the phone`() =
        runTest {
            val viewModel = TicketViewModel(FakeTicketsRepo(testTicket()), dayOne, nairobi, SavedStateHandle())
            collect(viewModel)

            viewModel.onAction(TicketAction.Remove)

            assertNull(ready(viewModel).ticket)
        }

    private fun TestScope.collect(viewModel: TicketViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    }

    private fun TestScope.ready(viewModel: TicketViewModel): TicketUiState.Ready {
        collect(viewModel)
        return viewModel.uiState.value as TicketUiState.Ready
    }
}