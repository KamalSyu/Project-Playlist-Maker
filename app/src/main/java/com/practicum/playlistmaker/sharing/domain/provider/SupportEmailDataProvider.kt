package com.practicum.playlistmaker.sharing.domain.provider

import com.practicum.playlistmaker.sharing.domain.model.SupportEmailIntentData

interface SupportEmailDataProvider {
    fun getEmailData(): SupportEmailIntentData
}