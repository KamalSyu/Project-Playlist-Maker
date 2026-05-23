package com.practicum.playlistmaker.core.models

data class Playlist(
    val id: String,
    val name: String,
    val coverPath: String? = null,
    val trackCount: Int = 0,
    val description: String? = null,
    val createdAt: Long = 0L,
    val trackIds: String? = null
)