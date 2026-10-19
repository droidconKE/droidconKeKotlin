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
package com.android254.presentation.common.livesessions

import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.models.SessionPresentationModel
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeaturedSessionTest {
    private val live = fakeSessions[0].copy(id = "live", isStarred = false)
    private val next = fakeSessions[1].copy(id = "next", isStarred = false)

    @Test
    fun `with nothing starred the live session leads`() {
        assertEquals("live", persistentListOf(live, next).featured()?.id)
    }

    @Test
    fun `your starred session leads, even when it is only up next`() {
        assertEquals("next", persistentListOf(live, next.copy(isStarred = true)).featured()?.id)
    }

    @Test
    fun `nothing on means no nudge`() {
        assertNull(persistentListOf<SessionPresentationModel>().featured())
    }
}