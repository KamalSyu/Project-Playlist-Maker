class Track {
    var trackName: String
    var artistName: String
    var trackTimeMillis: Long
    var artworkUrl100: String

    constructor(trackName: String, artistName: String, trackTimeMillis: Long, artworkUrl100: String) {
        this.trackName = trackName
        this.artistName = artistName
        this.trackTimeMillis = trackTimeMillis
        this.artworkUrl100 = artworkUrl100
    }
}
