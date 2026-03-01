package com.practicum.playlistmaker.settings.data.mapper

import com.practicum.playlistmaker.settings.domain.model.ThemeSettings
import com.practicum.playlistmaker.settings.data.dto.ThemeSettingsDTO
import javax.inject.Inject

class ThemeSettingsMapper @Inject constructor() {

    fun fromDto(dto: ThemeSettingsDTO): ThemeSettings =
        ThemeSettings(isDarkTheme = dto.isDarkTheme)

    fun toDto(domain: ThemeSettings): ThemeSettingsDTO =
        ThemeSettingsDTO(isDarkTheme = domain.isDarkTheme)
}