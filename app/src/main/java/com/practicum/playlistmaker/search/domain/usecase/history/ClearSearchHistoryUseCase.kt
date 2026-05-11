package com.practicum.playlistmaker.search.domain.usecase.history

interface ClearSearchHistoryUseCase {
    suspend operator fun invoke()
}