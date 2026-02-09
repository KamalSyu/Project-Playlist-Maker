package com.practicum.playlistmaker.data.provider

import com.practicum.playlistmaker.domain.usecase.DelayProvider

class CoroutineDelayProvider : DelayProvider {
    override suspend fun delay(millis: Long) {
        kotlinx.coroutines.delay(millis)
    }
}
