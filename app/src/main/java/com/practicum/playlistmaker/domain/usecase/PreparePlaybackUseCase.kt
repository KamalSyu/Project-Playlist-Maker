package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject

class PreparePlaybackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository) : PreparePlaybackUseCaseContract {

    override suspend operator fun invoke(previewUrl: String?): Result<Unit> {
        return try {
            playerRepository.prepare(previewUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
