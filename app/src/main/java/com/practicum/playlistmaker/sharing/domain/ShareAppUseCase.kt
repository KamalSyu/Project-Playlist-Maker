package com.practicum.playlistmaker.sharing.domain

import com.practicum.playlistmaker.ShareAppUseCaseContract
import com.practicum.playlistmaker.ShareTextProvider
import javax.inject.Inject

class ShareAppUseCase @Inject constructor(
    private val shareTextProvider: ShareTextProvider
) : ShareAppUseCaseContract {

    override operator fun invoke(): String {
        return shareTextProvider.getShareText()
    }
}