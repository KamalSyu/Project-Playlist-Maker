package com.practicum.playlistmaker.core.utils

import com.practicum.playlistmaker.DelayProvider

class CoroutineDelayProvider : DelayProvider {
    override suspend fun delay(millis: Long) {
        kotlinx.coroutines.delay(millis)
    }
}