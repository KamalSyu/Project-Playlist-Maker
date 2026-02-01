package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.SettingsRepository
import javax.inject.Inject

class GetThemeStateUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : GetThemeStateUseCaseContract {

    override operator fun invoke(): Boolean {
        return settingsRepository.isDarkThemeEnabled()
    }
}