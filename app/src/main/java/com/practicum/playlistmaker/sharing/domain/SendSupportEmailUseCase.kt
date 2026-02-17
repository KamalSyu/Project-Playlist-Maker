package com.practicum.playlistmaker.sharing.domain

import com.practicum.playlistmaker.sharing.domain.model.SupportEmailIntentData
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider
import com.practicum.playlistmaker.core.contract.SendSupportEmailUseCaseContract
import javax.inject.Inject

class SendSupportEmailUseCase @Inject constructor(
    private val supportEmailDataProvider: SupportEmailDataProvider
) : SendSupportEmailUseCaseContract {

    override operator fun invoke(): SupportEmailIntentData {
        return supportEmailDataProvider.getEmailData()
    }
}