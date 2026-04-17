package com.practicum.playlistmaker.player.ui.adapter

sealed interface PlaybackPayload

object UpdatePlaybackStatePayload : PlaybackPayload
