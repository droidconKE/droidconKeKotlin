/*
 * Copyright 2023 DroidconKE
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
package com.android254.presentation.sessions.mappers

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TimePeriodTest(
    private val input: String,
    private val timeResult: String,
    private val periodResult: String,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("2000-01-23 23:56:44", "11:56", "pm"),
                arrayOf("2023-12-10 10:02:02", "10:02", "am"),
                arrayOf("2005-02-14 00:00:00", "12:00", "am"),
                arrayOf("1996-01-23 12:18:44", "12:18", "pm"),
                arrayOf("2010-04-30 24:00:00", "12:00", "am"),
                arrayOf("2001-01-23 18:12:02", "06:12", "pm"),
                arrayOf("1998-01-23 04:09:19", "04:09", "am"),
                arrayOf("2022-01-23 21:21:05", "09:21", "pm"),
                arrayOf("2003-01-23 10:56:44", "10:56", "am"),
                arrayOf("1990-01-23 16:12:52", "04:12", "pm"),
            )
        }
    }

    @Test
    fun `given valid date string should return valid FormattedTime`() {
        val formattedTime: FormattedTime = getTimePeriod(time = input)
        assertEquals(
            "Should be valid time",
            timeResult,
            formattedTime.time,
        )
        assertEquals(
            "Should be valid period",
            periodResult.lowercase(),
            formattedTime.period.lowercase(),
        )
    }
}