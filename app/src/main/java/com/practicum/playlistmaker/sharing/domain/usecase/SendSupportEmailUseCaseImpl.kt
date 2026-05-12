package com.practicum.playlistmaker.sharing.domain.usecase

import com.practicum.playlistmaker.sharing.domain.model.SupportEmailIntentData
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider


class SendSupportEmailUseCaseImpl (
    private val supportEmailDataProvider: SupportEmailDataProvider
) : SendSupportEmailUseCase {

    override operator fun invoke(): SupportEmailIntentData {
        return supportEmailDataProvider.getEmailData()
    }
}