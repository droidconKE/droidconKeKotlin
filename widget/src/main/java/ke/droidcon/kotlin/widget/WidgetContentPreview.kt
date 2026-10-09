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

import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.android254.domain.models.Session
import com.android254.domain.models.Speaker

/**
 * Studio-only previews for [WidgetContent]'s states, rendered at this widget's three [DpSize]
 * breakpoints (see `NextSessionWidget`'s `SmallWidget`/`MediumWidget`/`LargeWidget`). Each
 * preview reads [LocalSize] the same way `NextSessionWidget.provideGlance` does, so the size
 * each `@Preview(widthDp, heightDp)` sets is what [WidgetContent] actually renders with.
 *
 * Kept in the codebase on purpose, for fast iteration in Android Studio when touching
 * [WidgetContent] — this is a deliberate deviation from the plan's Task 5 Step 1, which
 * described a similar preview as dev-only scaffolding to delete before committing.
 */
private fun previewSession(
    title: String,
    rooms: String = "Hall A",
    startTime: String = "10:00 AM",
    endTime: String = "",
    speakers: List<Speaker> = emptyList(),
) = Session(
    id = "preview",
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
    remoteId = "preview",
    description = "",
    sessionFormat = "",
    sessionLevel = "",
    slug = "",
    title = title,
    eventDay = "",
)

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 140, heightDp = 100)
@Preview(widthDp = 250, heightDp = 100)
@Preview(widthDp = 250, heightDp = 200)
@Composable
private fun WidgetContentHappeningNowPreview() {
    GlanceTheme {
        WidgetContent(
            current = listOf(previewSession(title = "Kotlin for busy engineers", rooms = "Hall A")),
            next = listOf(previewSession(title = "Should not show")),
            size = LocalSize.current,
            launchIntent = null,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 140, heightDp = 100)
@Preview(widthDp = 250, heightDp = 100)
@Preview(widthDp = 250, heightDp = 200)
@Composable
private fun WidgetContentUpNextPreview() {
    GlanceTheme {
        WidgetContent(
            current = emptyList(),
            next = listOf(previewSession(title = "Scaling droidcon KE", rooms = "Hall B", startTime = "2:00 PM")),
            size = LocalSize.current,
            launchIntent = null,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 140, heightDp = 100)
@Preview(widthDp = 250, heightDp = 100)
@Preview(widthDp = 250, heightDp = 200)
@Composable
private fun WidgetContentConcurrentSessionsPreview() {
    GlanceTheme {
        WidgetContent(
            current =
                listOf(
                    previewSession(title = "Kotlin for busy engineers", rooms = "Track 1"),
                    previewSession(title = "Scaling droidcon KE", rooms = "Track 2"),
                    previewSession(title = "Lightning talks", rooms = "Track 3"),
                ),
            next = emptyList(),
            size = LocalSize.current,
            launchIntent = null,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 200)
@Composable
private fun WidgetContentHappeningNowWithSpeakerLargePreview() {
    GlanceTheme {
        WidgetContent(
            current =
                listOf(
                    previewSession(
                        title = "Building Resilient Systems",
                        rooms = "Opal",
                        endTime = "11:30 AM",
                        speakers = listOf(Speaker(name = "Ada Lovelace")),
                    ),
                ),
            next = emptyList(),
            size = LocalSize.current,
            launchIntent = null,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 140, heightDp = 100)
@Preview(widthDp = 250, heightDp = 100)
@Preview(widthDp = 250, heightDp = 200)
@Composable
private fun WidgetContentUpNextOpalRoomPreview() {
    GlanceTheme {
        WidgetContent(
            current = emptyList(),
            next =
                listOf(
                    previewSession(
                        title = "The Future of Kotlin",
                        rooms = "Opal",
                        startTime = "2:00 PM",
                        speakers = listOf(Speaker(name = "Grace Hopper")),
                    ),
                ),
            size = LocalSize.current,
            launchIntent = null,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 140, heightDp = 100)
@Preview(widthDp = 250, heightDp = 100)
@Preview(widthDp = 250, heightDp = 200)
@Composable
private fun WidgetContentUpNextSapphireRoomPreview() {
    GlanceTheme {
        WidgetContent(
            current = emptyList(),
            next =
                listOf(
                    previewSession(
                        title = "Designing for Delight",
                        rooms = "Sapphire",
                        startTime = "3:30 PM",
                        speakers = listOf(Speaker(name = "Margaret Hamilton")),
                    ),
                ),
            size = LocalSize.current,
            launchIntent = null,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 140, heightDp = 100)
@Preview(widthDp = 250, heightDp = 100)
@Preview(widthDp = 250, heightDp = 200)
@Composable
private fun WidgetContentEmptyPreview() {
    GlanceTheme {
        WidgetContent(
            current = emptyList(),
            next = emptyList(),
            size = LocalSize.current,
            launchIntent = null,
        )
    }
}