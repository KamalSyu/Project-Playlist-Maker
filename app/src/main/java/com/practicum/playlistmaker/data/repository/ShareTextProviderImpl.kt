package com.practicum.playlistmaker.data.repository

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.usecase.ShareTextProvider


class ShareTextProviderImpl(
    private val context: Context
) : ShareTextProvider {

    override fun getShareText(): String {
        return context.getString(R.string.share_text)
    }
}