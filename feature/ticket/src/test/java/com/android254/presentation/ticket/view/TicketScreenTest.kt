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
package com.android254.presentation.ticket.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.android254.domain.models.Ticket
import com.android254.presentation.ticket.AddTicketError
import com.android254.presentation.ticket.AddTicketForm
import com.android254.presentation.ticket.TicketAction
import com.android254.presentation.ticket.TicketUiState
import com.android254.presentation.ticket.toUi
import com.droidconke.chai.ChaiTheme
import ke.droidcon.kotlin.core.testing.testTicket
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w360dp-h780dp-xxhdpi")
class TicketScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `adding stays off until both fields are filled`() {
        show(ready(form = AddTicketForm(number = "TIC-70235")))

        composeTestRule.onNodeWithText("Add ticket").assertIsNotEnabled()
    }

    @Test
    fun `a failed lookup says why`() {
        show(ready(form = AddTicketForm(number = "TIC-1", email = "a@b.c", error = AddTicketError.Offline)))

        composeTestRule.onNodeWithText("Add ticket").assertIsEnabled()
        composeTestRule.onNodeWithText("You’re offline", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `scanning asks for the camera from the form`() {
        val actions = mutableListOf<TicketAction>()
        show(ready(), actions::add)

        composeTestRule.onNodeWithText("Scan a printed ticket").performScrollTo().performClick()

        assertEquals(listOf(TicketAction.Scan), actions)
    }

    @Test
    fun `a saved ticket shows its code with the number to read out`() {
        show(ready(ticket = testTicket()))

        composeTestRule.onNodeWithContentDescription("Entry code for ticket TIC-70235").assertIsDisplayed()
        composeTestRule.onNodeWithText("TIC-70235").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show this at the entrance").assertIsDisplayed()
    }

    @Test
    fun `a refunded ticket is stamped and says it will not scan`() {
        show(ready(ticket = testTicket(status = Ticket.Status.Refunded)))

        composeTestRule.onNodeWithText("REFUNDED").assertIsDisplayed()
        composeTestRule.onNodeWithText("won’t scan", substring = true).assertIsDisplayed()
    }

    @Test
    fun `a code that will not decode tells the attendee to read the number out`() {
        show(ready(ticket = testTicket().copy(qrBase64 = "")))

        composeTestRule.onNodeWithText("Read your ticket number", substring = true).assertIsDisplayed()
    }

    @Test
    fun `removing is behind the menu and asks first`() {
        val actions = mutableListOf<TicketAction>()
        show(ready(ticket = testTicket()), actions::add)

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Remove ticket").performClick()
        composeTestRule.onNodeWithText("Remove ticket TIC-70235?").assertIsDisplayed()
        composeTestRule.onNode(hasText("Remove") and hasAnyAncestor(isDialog())).performClick()

        assertEquals(listOf(TicketAction.Remove), actions)
    }

    private fun show(
        state: TicketUiState,
        onAction: (TicketAction) -> Unit = {},
    ) {
        composeTestRule.setContent {
            ChaiTheme { TicketScreen(state = state, onBack = {}, onAction = onAction) }
        }
    }

    private fun ready(
        ticket: Ticket? = null,
        form: AddTicketForm = AddTicketForm(),
    ) = TicketUiState.Ready(
        ticket = ticket?.toUi(Instant.parse("2026-11-05T07:00:00Z"), TimeZone.of("Africa/Nairobi")),
        form = form,
    )
}