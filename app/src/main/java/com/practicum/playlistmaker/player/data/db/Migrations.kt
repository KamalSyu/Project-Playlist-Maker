//package com.practicum.playlistmaker.player.data.db
//
//import androidx.room.migration.Migration
//import androidx.sqlite.db.SupportSQLiteDatabase
//
//val MIGRATION_1_2 = object : Migration(1, 2) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("""
//            CREATE TABLE IF NOT EXISTS favorite_tracks (
//                trackId TEXT NOT NULL,
//                trackName TEXT NOT NULL,
//                artistName TEXT NOT NULL,
//                artworkUrl100 TEXT,
//                previewUrl TEXT,
//                collectionName TEXT,
//                releaseDate TEXT,
//                primaryGenreName TEXT,
//                country TEXT,
//                trackTimeMillis INTEGER,
//                addedAt INTEGER NOT NULL,
//                PRIMARY KEY (trackId)
//            )
//        """)
//    }
//}
