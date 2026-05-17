package com.practicum.playlistmaker.mediateka.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val description: String,
    val coverPath: String?,
    val trackIds: String,
    val trackCount: Int,
    val createdAt: Long
)
