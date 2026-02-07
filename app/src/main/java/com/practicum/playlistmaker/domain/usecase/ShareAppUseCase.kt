package com.practicum.playlistmaker.domain.usecase


import javax.inject.Inject

class ShareAppUseCase @Inject constructor(
    private val shareTextProvider: ShareTextProvider
) : ShareAppUseCaseContract {

    override operator fun invoke(): String {
        return shareTextProvider.getShareText()
    }
}