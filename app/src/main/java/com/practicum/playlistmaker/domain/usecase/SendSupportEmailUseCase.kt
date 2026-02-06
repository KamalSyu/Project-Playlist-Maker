package com.practicum.playlistmaker.domain.usecase

import android.content.Context
import com.practicum.playlistmaker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SendSupportEmailUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) : SendSupportEmailUseCaseContract{

    override operator fun invoke(): SupportEmailIntentData {
        return SupportEmailIntentData(
            email = context.getString(R.string.support_email),
            subject = context.getString(R.string.email_subject),
            body = context.getString(R.string.email_text)
        )
    }
}

data class SupportEmailIntentData(
    val email: String,
    val subject: String,
    val body: String
)
