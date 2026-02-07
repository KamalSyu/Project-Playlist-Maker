package com.practicum.playlistmaker.domain.model

data class SupportEmailIntentData(
    val email: String,
    val subject: String,
    val body: String
)