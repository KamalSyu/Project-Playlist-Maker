package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import com.practicum.playlistmaker.core.contract.PreparePlaybackUseCaseContract
import javax.inject.Inject

class PreparePlaybackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) : PreparePlaybackUseCaseContract {

    override suspend operator fun invoke(previewUrl: String?): Result<Unit> {
        return try {
            playerRepository.prepare(previewUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}