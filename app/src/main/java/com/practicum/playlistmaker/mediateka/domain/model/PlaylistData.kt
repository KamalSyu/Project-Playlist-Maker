package com.practicum.playlistmaker.mediateka.domain.model

data class PlaylistData(
    val id: Long = 0,
    val name: String,
    val description: String,
    val coverPath: String?,
    val trackIds: String = "[]",
    val trackCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)