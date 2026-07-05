package com.practicum.playlistmaker.core.models.domain

sealed interface AddTrackStatus {
    object SUCCESS : AddTrackStatus
    object ALREADY_EXISTS : AddTrackStatus
    object ERROR : AddTrackStatus
}
