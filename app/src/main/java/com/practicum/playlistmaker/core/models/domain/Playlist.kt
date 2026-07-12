package com.practicum.playlistmaker.core.models.domain

data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val coverPath: String?,
    val trackCount: Int,
    val createdAt: Long,
    val durationFormatted: String = "00:00"
)