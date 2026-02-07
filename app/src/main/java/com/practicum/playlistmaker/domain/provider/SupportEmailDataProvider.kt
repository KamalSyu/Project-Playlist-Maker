package com.practicum.playlistmaker.domain.provider

import com.practicum.playlistmaker.domain.model.SupportEmailIntentData

interface SupportEmailDataProvider {
    fun getEmailData(): SupportEmailIntentData
}
