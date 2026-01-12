class Track {
    val trackId: Int
    var trackName: String
    var artistName: String
    var trackTimeMillis: Long
    var artworkUrl100: String

    constructor(trackId: Int, trackName: String, artistName: String, trackTimeMillis: Long, artworkUrl100: String) {
        this.trackId = trackId
        this.trackName = trackName
        this.artistName = artistName
        this.trackTimeMillis = trackTimeMillis
        this.artworkUrl100 = artworkUrl100
    }

    constructor(trackName: String, artistName: String, trackTimeMillis: Long, artworkUrl100: String) : this(0, trackName, artistName, trackTimeMillis, artworkUrl100)
}

