package com.practicum.playlistmaker.domain.usecase

import javax.inject.Inject

class ShareAppUseCase @Inject constructor(){
    operator fun invoke(): String {
        return "Скачайте приложение: https://example.com/playlistmaker"
    }
}
