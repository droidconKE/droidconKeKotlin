package ke.droidcon.kotlin.datasource.remote.feed.model

import java.time.LocalDateTime
import ke.droidcon.kotlin.datasource.remote.feed.deserializer.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedDTO(
    val title: String,
    val body: String,
    val topic: String,
    val url: String,
    val image: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("created_at")
    val createdAt: LocalDateTime
)