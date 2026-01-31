package com.practicum.playlistmaker.domain.usecase

import javax.inject.Inject

class SendSupportEmailUseCase @Inject constructor() : SendSupportEmailUseCaseContract{

    override operator fun invoke(): SupportEmailIntentData {
        return SupportEmailIntentData(
            email = "support@example.com",
            subject = "Вопрос по приложению Playlist Maker",
            body = "Здравствуйте! У меня возникла проблема..."
        )
    }
}

data class SupportEmailIntentData(
    val email: String,
    val subject: String,
    val body: String
)
