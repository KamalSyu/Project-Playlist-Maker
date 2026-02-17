package com.practicum.playlistmaker.sharing.domain.provider

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.contract.ShareTextProvider

class ShareTextProviderImpl(
    private val context: Context
) : ShareTextProvider {

    override fun getShareText(): String {
        return context.getString(R.string.share_text)
    }
}