package com.practicum.playlistmaker.core.utils


fun Int.toTracksText(): String {
    return when {
        this % 10 == 1 && this % 100 != 11 ->
            "$this трек"

        this % 10 in 2..4 && this % 100 !in 12..14 ->
            "$this трека"

        else ->
            "$this треков"
    }
}