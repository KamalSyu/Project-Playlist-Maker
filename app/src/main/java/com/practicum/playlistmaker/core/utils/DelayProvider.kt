package com.practicum.playlistmaker.core.utils

interface DelayProvider {
    suspend fun delay(millis: Long)
}