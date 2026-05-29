package com.practicum.playlistmaker.player.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.practicum.playlistmaker.mediateka.data.db.PlaylistEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao

@Database(
    entities = [FavoriteTrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class],
    version = 2,  // увеличили версию с 1 до 2
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao
    abstract fun playlistsDao(): PlaylistsDao

}
