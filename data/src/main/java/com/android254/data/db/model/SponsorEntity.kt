package com.android254.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName

@Entity(tableName = "sponsors")
data class SponsorEntity(
    // We are using this as a primary key since the API does not return an ID so we can be able to use the mappers
    @PrimaryKey
    val name: String,
    val tagline: String,
    val link: String,
    val logo: String,
    var createdAt: String

)