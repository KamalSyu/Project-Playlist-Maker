package com.practicum.playlistmaker.data.repository

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.model.SupportEmailIntentData
import com.practicum.playlistmaker.domain.provider.SupportEmailDataProvider

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