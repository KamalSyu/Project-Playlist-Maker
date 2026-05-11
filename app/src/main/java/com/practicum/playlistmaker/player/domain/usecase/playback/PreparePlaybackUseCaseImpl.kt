package com.practicum.playlistmaker.player.domain.usecase.playback

import com.practicum.playlistmaker.core.contract.PreparePlaybackUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class PreparePlaybackUseCaseImpl (
    private val playerRepository: PlayerRepository
) : PreparePlaybackUseCase {

    override suspend operator fun invoke(previewUrl: String?): Result<Unit> {
        return try {
            playerRepository.prepare(previewUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}