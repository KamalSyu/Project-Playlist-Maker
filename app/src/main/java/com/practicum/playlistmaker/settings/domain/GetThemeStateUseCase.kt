package com.practicum.playlistmaker.settings.domain

import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository
import com.practicum.playlistmaker.core.contract.GetThemeStateUseCaseContract
import javax.inject.Inject

class GetThemeStateUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : GetThemeStateUseCaseContract {

    override operator fun invoke(): Boolean {
        return settingsRepository.getThemeSettings().isDarkTheme
    }
}