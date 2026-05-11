package com.practicum.playlistmaker.core.utils


class CoroutineDelayProvider : DelayProvider {
    override suspend fun delay(millis: Long) {
        kotlinx.coroutines.delay(millis)
    }
}