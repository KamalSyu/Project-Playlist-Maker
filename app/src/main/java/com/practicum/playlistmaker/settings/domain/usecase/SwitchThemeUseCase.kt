package com.practicum.playlistmaker.settings.domain.usecase

interface SwitchThemeUseCase {
    operator fun invoke(isDarkMode: Boolean)
}