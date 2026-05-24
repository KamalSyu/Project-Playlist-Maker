package com.practicum.playlistmaker.core.models.domain

sealed class AddTrackStatus {
    object SUCCESS : AddTrackStatus()
    object ALREADY_EXISTS : AddTrackStatus()
    object ERROR : AddTrackStatus()
}