package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.core.contract.GetPlaybackPositionUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import javax.inject.Inject

class GetPlaybackPositionUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : GetPlaybackPositionUseCaseContract {


    override operator  fun invoke(
        isPlaying: Boolean,
        savedPosition: Long,
        resetTime: Boolean
    ): Long {
        return when {
            resetTime -> 0L
            !isPlaying && savedPosition > 0L -> savedPosition
            else -> playerRepository.getCurrentPosition()
        }
    }
}
