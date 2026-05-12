package com.practicum.playlistmaker.sharing.domain.usecase

import com.practicum.playlistmaker.sharing.domain.provider.ShareTextProvider

class ShareAppUseCaseImpl (
    private val shareTextProvider: ShareTextProvider
) : ShareAppUseCase {

    override operator fun invoke(): String {
        return shareTextProvider.getShareText()
    }
}