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
package com.android254.presentation.sessionDetails.view.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SpeakerTwitterHandleTest {
    @Test
    fun `strips leading at-sign and trims whitespace`() {
        val uri = twitterProfileUri("  @droidconke  ")

        assertEquals("https://www.twitter.com/droidconke", uri.toString())
    }

    @Test
    fun `encodes special characters as a single path segment`() {
        val uri = twitterProfileUri("user?redirect=evil#frag")

        assertEquals("https", uri?.scheme)
        assertEquals("www.twitter.com", uri?.authority)
        assertNull(uri?.query)
        assertNull(uri?.fragment)
    }

    @Test
    fun `returns null for blank or bare at-sign handle`() {
        assertNull(twitterProfileUri(""))
        assertNull(twitterProfileUri("   "))
        assertNull(twitterProfileUri("@"))
    }
}