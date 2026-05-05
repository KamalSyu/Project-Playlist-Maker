package com.practicum.playlistmaker.sharing.domain.usecase

import com.practicum.playlistmaker.core.contract.SendSupportEmailUseCaseContract
import com.practicum.playlistmaker.sharing.domain.model.SupportEmailIntentData
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider


class SendSupportEmailUseCase (
    private val supportEmailDataProvider: SupportEmailDataProvider
) : SendSupportEmailUseCaseContract {

    override operator fun invoke(): SupportEmailIntentData {
        return supportEmailDataProvider.getEmailData()
    }
}