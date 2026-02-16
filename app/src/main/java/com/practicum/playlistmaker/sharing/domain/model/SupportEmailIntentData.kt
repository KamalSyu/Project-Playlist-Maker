package com.practicum.playlistmaker.sharing.domain.model

data class SupportEmailIntentData(
    val email: String,
    val subject: String,
    val body: String
)