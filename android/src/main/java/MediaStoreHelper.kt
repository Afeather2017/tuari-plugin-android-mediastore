package com.plugin.android.mediastore

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log

data class AudioFileData(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val contentUri: String,
    val firstFourBytes: String?
)

class MediaStoreHelper(private val context: Context) {
    private val TAG = "MediaStoreHelper"

    fun getAudioFiles(): List<AudioFileData> {
        val audioFiles = mutableListOf<AudioFileData>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = ?"
        val selectionArgs = arrayOf("1")

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown"
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val album = it.getString(albumColumn) ?: "Unknown Album"
                    val duration = it.getLong(durationColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
                    val firstFourBytes = readFirstFourBytes(contentUri)

                    audioFiles.add(AudioFileData(id, title, artist, album, duration, contentUri, firstFourBytes))
                }
            }

            Log.d(TAG, "Found ${audioFiles.size} audio files")
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore", e)
        }

        return audioFiles
    }

    private fun readFirstFourBytes(contentUriString: String): String? {
        return try {
            val uri = android.net.Uri.parse(contentUriString)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = ByteArray(4)
                val bytesRead = inputStream.read(bytes)
                if (bytesRead > 0) {
                    bytes.joinToString("") { "%02x".format(it) }.uppercase()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading file bytes for $contentUriString", e)
            null
        }
    }
}
