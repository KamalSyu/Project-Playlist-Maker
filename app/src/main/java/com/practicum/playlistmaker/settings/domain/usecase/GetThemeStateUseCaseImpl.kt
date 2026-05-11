package com.practicum.playlistmaker.settings.domain.usecase

import com.practicum.playlistmaker.core.contract.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository

class GetThemeStateUseCaseImpl (
    private val settingsRepository: SettingsRepository
) : GetThemeStateUseCase {

    override operator fun invoke(): Boolean {
        return settingsRepository.getThemeSettings().isDarkTheme
    }
}