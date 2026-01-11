
import android.util.Log
import java.io.Serializable

class Track: Serializable {
    val trackId: Int
    var trackName: String
    var artistName: String
    var trackTimeMillis: Long
    var artworkUrl100: String
    var releaseDate: String? = null
    var collectionName: String? = null
    var primaryGenreName: String? = null
    var country: String? = null
    var previewUrl: String? = null


    constructor(trackId: Int, trackName: String, artistName: String, trackTimeMillis: Long, artworkUrl100: String, previewUrl: String?) {
        this.trackId = trackId
        this.trackName = trackName
        this.artistName = artistName
        this.trackTimeMillis = trackTimeMillis
        this.artworkUrl100 = artworkUrl100
        this.previewUrl = previewUrl
    }

    constructor(trackName: String, artistName: String, trackTimeMillis: Long, artworkUrl100: String) : this(0, trackName, artistName, trackTimeMillis, artworkUrl100, null)

    fun getHighQualityArtworkUrl(): String {
        return artworkUrl100.replaceAfterLast("/", "512x512bb.jpg")
    }
}
