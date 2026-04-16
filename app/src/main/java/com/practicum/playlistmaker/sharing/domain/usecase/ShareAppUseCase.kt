package com.practicum.playlistmaker.sharing.domain.usecase

import com.practicum.playlistmaker.core.contract.ShareAppUseCaseContract
import com.practicum.playlistmaker.sharing.domain.provider.ShareTextProvider

class ShareAppUseCase (
    private val shareTextProvider: ShareTextProvider
) : ShareAppUseCaseContract {

    override operator fun invoke(): String {
        return shareTextProvider.getShareText()
    }
}