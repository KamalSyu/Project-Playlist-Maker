package com.practicum.playlistmaker.sharing.domain

import com.practicum.playlistmaker.core.contract.ShareAppUseCaseContract
import com.practicum.playlistmaker.core.contract.ShareTextProvider
import javax.inject.Inject

class ShareAppUseCase @Inject constructor(
    private val shareTextProvider: ShareTextProvider
) : ShareAppUseCaseContract {

    override operator fun invoke(): String {
        return shareTextProvider.getShareText()
    }
}