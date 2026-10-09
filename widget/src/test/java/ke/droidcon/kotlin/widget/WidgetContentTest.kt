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
package ke.droidcon.kotlin.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.EmittableWithText
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.GlanceNodeMatcher
import androidx.glance.testing.unit.MappedNode
import androidx.glance.testing.unit.assertHasText
import androidx.glance.testing.unit.hasTestTag
import androidx.test.core.app.ApplicationProvider
import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WidgetContentTest {
    // glance-testing has no public assertHasMaxLines helper; its own hasText()/hasTextEqualTo()
    // matchers (UnitTestFilters.kt) reach into the same RestrictTo(LIBRARY_GROUP) Emittable type
    // to read node state, so this mirrors that pattern to check the one property those helpers
    // don't expose.
    @Suppress("RestrictedApi")
    private val hasMaxLinesOfOne =
        GlanceNodeMatcher<MappedNode>("maxLines == 1") { node ->
            val emittable = node.value.emittable
            emittable is EmittableWithText && emittable.maxLines == 1
        }

    private fun fakeSession(
        id: String = "1",
        title: String = "Test Session",
        rooms: String = "Hall A",
        startTime: String = "10:00 AM",
        endTime: String = "",
        speakers: List<Speaker> = emptyList(),
    ) = Session(
        id = id,
        endDateTime = "",
        endTime = endTime,
        isBookmarked = false,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = null,
        startDateTime = "",
        startTime = startTime,
        rooms = rooms,
        speakers = speakers,
        remoteId = id,
        description = "",
        sessionFormat = "",
        sessionLevel = "",
        slug = "",
        title = title,
        eventDay = "",
    )

    private fun fakeSpeaker(name: String) = Speaker(name = name)

    @Test
    fun widgetContent_currentOnly_showsLiveSession() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(
                    current = listOf(fakeSession(title = "Keynote")),
                    next = emptyList(),
                    size = DpSize(140.dp, 100.dp),
                    launchIntent = null,
                )
            }
            onNode(hasTestTag("statusLabel")).assertHasText("LIVE")
            onNode(hasTestTag("sessionTitle")).assertHasText("Keynote")
        }

    @Test
    fun widgetContent_nextOnly_showsUpNextSession() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(
                    current = emptyList(),
                    next = listOf(fakeSession(title = "Workshop")),
                    size = DpSize(250.dp, 100.dp),
                    launchIntent = null,
                )
            }
            onNode(hasTestTag("statusLabel")).assertHasText("UP NEXT")
            onNode(hasTestTag("sessionTitle")).assertHasText("Workshop")
        }

    @Test
    fun widgetContent_noSessions_showsEmptyState() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(current = emptyList(), next = emptyList(), size = DpSize(140.dp, 100.dp), launchIntent = null)
            }
            onNode(hasTestTag("emptyState")).assertExists()
        }

    @Test
    fun widgetContent_longRoomName_boundsDetailLineToOneLine() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(
                    current =
                        listOf(
                            fakeSession(
                                title = "Keynote",
                                rooms = "A very long room name that would otherwise wrap across several lines",
                            ),
                        ),
                    next = emptyList(),
                    size = DpSize(140.dp, 100.dp),
                    launchIntent = null,
                )
            }
            onNode(hasTestTag("sessionDetail")).assert(hasMaxLinesOfOne)
        }

    @Test
    fun widgetContent_currentAndNext_showsBothNotHidden() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(
                    current = listOf(fakeSession(id = "c1", title = "Keynote")),
                    next = listOf(fakeSession(id = "n1", title = "Workshop")),
                    size = DpSize(250.dp, 200.dp),
                    launchIntent = null,
                )
            }
            onAllNodes(hasTestTag("sessionTitle")).assertCountEquals(2)
            onAllNodes(hasTestTag("sessionTitle")).get(0).assertHasText("Keynote")
            onAllNodes(hasTestTag("sessionTitle")).get(1).assertHasText("Workshop")
            onAllNodes(hasTestTag("statusLabel")).get(0).assertHasText("LIVE")
            onAllNodes(hasTestTag("statusLabel")).get(1).assertHasText("UP NEXT")
        }

    @Test
    fun widgetContent_multipleCurrentSessions_showsAllAsRows() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(
                    current =
                        listOf(
                            fakeSession(id = "c1", title = "Keynote"),
                            fakeSession(id = "c2", title = "Parallel track talk"),
                        ),
                    next = emptyList(),
                    size = DpSize(250.dp, 200.dp),
                    launchIntent = null,
                )
            }
            onAllNodes(hasTestTag("sessionTitle")).assertCountEquals(2)
            onAllNodes(hasTestTag("sessionTitle")).get(0).assertHasText("Keynote")
            onAllNodes(hasTestTag("sessionTitle")).get(1).assertHasText("Parallel track talk")
        }

    @Test
    fun widgetContent_multipleNextSessions_showsAllAsRows() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(
                    current = emptyList(),
                    next =
                        listOf(
                            fakeSession(id = "n1", title = "Workshop"),
                            fakeSession(id = "n2", title = "Panel"),
                            fakeSession(id = "n3", title = "Lightning talks"),
                        ),
                    size = DpSize(250.dp, 200.dp),
                    launchIntent = null,
                )
            }
            onAllNodes(hasTestTag("sessionTitle")).assertCountEquals(3)
            onAllNodes(hasTestTag("sessionTitle")).get(0).assertHasText("Workshop")
            onAllNodes(hasTestTag("sessionTitle")).get(1).assertHasText("Panel")
            onAllNodes(hasTestTag("sessionTitle")).get(2).assertHasText("Lightning talks")
        }

    @Test
    fun widgetContent_largeBreakpointCurrentSessionWithSpeaker_detailIncludesSpeakerAndEndTime() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(
                    current =
                        listOf(
                            fakeSession(
                                title = "Keynote",
                                rooms = "Hall A",
                                endTime = "11:30 AM",
                                speakers = listOf(fakeSpeaker("Ada Lovelace")),
                            ),
                        ),
                    next = emptyList(),
                    size = DpSize(250.dp, 200.dp),
                    launchIntent = null,
                )
            }
            onNode(hasTestTag("sessionDetail")).assertHasText("Ada Lovelace · Hall A · ends 11:30 AM")
        }

    @Test
    fun widgetContent_largeBreakpointNextSessionWithSpeaker_detailIncludesSpeakerAndStartTime() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(
                    current = emptyList(),
                    next =
                        listOf(
                            fakeSession(
                                title = "Workshop",
                                rooms = "Hall B",
                                startTime = "2:00 PM",
                                speakers = listOf(fakeSpeaker("Grace Hopper")),
                            ),
                        ),
                    size = DpSize(250.dp, 200.dp),
                    launchIntent = null,
                )
            }
            onNode(hasTestTag("sessionDetail")).assertHasText("Grace Hopper · Hall B · 2:00 PM")
        }

    @Test
    fun widgetContent_largeBreakpointNoSpeaker_detailFallsBackToCompactText() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(
                    current = listOf(fakeSession(title = "Keynote", rooms = "Hall A")),
                    next = emptyList(),
                    size = DpSize(250.dp, 200.dp),
                    launchIntent = null,
                )
            }
            onNode(hasTestTag("sessionDetail")).assertHasText("Hall A")
        }

    @Test
    fun widgetContent_smallBreakpointWithSpeaker_detailStaysCompact() =
        runGlanceAppWidgetUnitTest {
            setContext(ApplicationProvider.getApplicationContext())
            provideComposable {
                WidgetContent(
                    current =
                        listOf(
                            fakeSession(
                                title = "Keynote",
                                rooms = "Hall A",
                                speakers = listOf(fakeSpeaker("Ada Lovelace")),
                            ),
                        ),
                    next = emptyList(),
                    size = DpSize(140.dp, 100.dp),
                    launchIntent = null,
                )
            }
            onNode(hasTestTag("sessionDetail")).assertHasText("Hall A")
        }
}