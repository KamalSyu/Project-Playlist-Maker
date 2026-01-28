package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.SearchResponseDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("search")
    suspend fun searchTracks(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song"
    ): SearchResponseDTO
}