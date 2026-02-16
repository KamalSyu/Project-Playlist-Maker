package com.practicum.playlistmaker.sharing.domain.provider

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.sharing.domain.model.SupportEmailIntentData
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider

class SupportEmailDataProviderImpl(
    private val context: Context
) : SupportEmailDataProvider {

    override fun getEmailData(): SupportEmailIntentData {
        return SupportEmailIntentData(
            email = context.getString(R.string.support_email),
            subject = context.getString(R.string.email_subject),
            body = context.getString(R.string.email_text)
        )
    }
}