package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.model.SupportEmailIntentData
import com.practicum.playlistmaker.domain.provider.SupportEmailDataProvider // ← из domain!
import javax.inject.Inject

class SendSupportEmailUseCase @Inject constructor(
    private val supportEmailDataProvider: SupportEmailDataProvider
) : SendSupportEmailUseCaseContract {

    override operator fun invoke(): SupportEmailIntentData {
        return supportEmailDataProvider.getEmailData()
    }
}

