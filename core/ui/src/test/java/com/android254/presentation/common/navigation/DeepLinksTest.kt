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
package com.android254.presentation.common.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepLinksTest {
    @Test
    fun `a session link opens that session's details`() {
        assertEquals(Screens.SessionDetails("abc123"), DeepLinks.parse("droidconke://session/abc123"))
    }

    @Test
    fun `a feed link opens the feed, with or without a post id`() {
        assertEquals(Screens.Feed, DeepLinks.parse("droidconke://feed"))
        assertEquals(Screens.Feed, DeepLinks.parse("droidconke://feed/post456"))
    }

    @Test
    fun `the links the app builds are the links it parses`() {
        assertEquals(Screens.SessionDetails("42"), DeepLinks.parse(DeepLinks.session("42")))
        assertEquals(Screens.Feed, DeepLinks.parse(DeepLinks.feed()))
    }

    @Test
    fun `a session link without an id goes nowhere`() {
        assertNull(DeepLinks.parse("droidconke://session"))
        assertNull(DeepLinks.parse("droidconke://session/"))
    }

    @Test
    fun `links outside the app's scheme or hosts go nowhere`() {
        assertNull(DeepLinks.parse("https://droidcon.co.ke/session/1"))
        assertNull(DeepLinks.parse("droidconke://speaker/1"))
        assertNull(DeepLinks.parse("not a uri"))
        assertNull(DeepLinks.parse(""))
    }
}