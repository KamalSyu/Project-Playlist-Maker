package com.practicum.playlistmaker.player.data.storage

import android.content.Context
import java.io.File
import java.io.IOException

class FileStorageService(private val context: Context) {

    private val privateDir: File = context.filesDir

    fun copyToPrivateStorage(sourcePath: String): String {
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) {
            throw IOException("Source file does not exist: $sourcePath")
        }

        val fileName = sourceFile.name
        val destinationFile = File(privateDir, fileName)

        sourceFile.copyTo(destinationFile, overwrite = true)
        return destinationFile.absolutePath
    }
}