package com.practicum.playlistmaker.sharing.domain.usecase

import com.practicum.playlistmaker.sharing.domain.model.SupportEmailIntentData

interface SendSupportEmailUseCase {
    operator fun invoke(): SupportEmailIntentData
}
