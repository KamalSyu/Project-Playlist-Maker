package com.practicum.playlistmaker.settings.data.mapper


import com.practicum.playlistmaker.settings.domain.model.ThemeSettings
import com.practicum.playlistmaker.settings.data.dto.ThemeSettingsDTO


/**
 * Конвертирует DTO настроек темы в доменную модель и обратно.
 */
class ThemeDtoMapper {

    /**
     * Преобразует DTO в доменную модель ThemeSettings.
     */
    fun toDomain(dto: ThemeSettingsDTO): ThemeSettings {
        return ThemeSettings(
            isDarkTheme = dto.isDarkTheme
        )
    }

    /**
     * Преобразует доменную модель ThemeSettings в DTO.
     */
    fun toDto(domain: ThemeSettings): ThemeSettingsDTO {
        return ThemeSettingsDTO(
            isDarkTheme = domain.isDarkTheme
        )
    }
}
