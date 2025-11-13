package com.practicum.playlistmaker

class Track {
    var trackName: String
    var artistName: String
    var trackTime: String
    var artworkUrl100: String

    constructor(trackName: String, artistName: String, trackTime: String, artworkUrl100: String) {
        this.trackName = trackName
        this.artistName = artistName
        this.trackTime = trackTime
        this.artworkUrl100 = artworkUrl100
    }
}
