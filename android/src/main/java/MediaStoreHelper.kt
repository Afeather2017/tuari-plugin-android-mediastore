package com.plugin.android.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import android.util.Base64

data class AudioFileData(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val contentUri: String,
    val firstFourBytes: String?
)

data class FileReaderSession(
    val sessionId: Long,
    val inputStream: InputStream,
    val contentUri: String,
    var position: Long = 0,
    val fileSize: Long?
)

data class ReadResult(
    val success: Boolean,
    val data: String?,
    val bytesRead: Int,
    val isEof: Boolean,
    val error: String?
)

data class SeekResult(
    val success: Boolean,
    val newPosition: Long,
    val error: String?
)

data class SessionInfo(
    val sessionId: Long,
    val contentUri: String,
    val position: Long,
    val fileSize: Long?,
    val isOpen: Boolean
)

class MediaStoreHelper(private val context: Context) {
    private val TAG = "MediaStoreHelper"

    companion object {
        private val fileReaders = ConcurrentHashMap<Long, FileReaderSession>()
        private val sessionIdCounter = AtomicLong(0)
    }

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

    fun openFileReader(contentUri: String): Pair<Long, Long?> {
        val sessionId = sessionIdCounter.incrementAndGet()
        var fileSize: Long? = null

        try {
            Log.d(TAG, "[openFileReader] Starting with contentUri: $contentUri")
            val uri = Uri.parse(contentUri)
            Log.d(TAG, "[openFileReader] Parsed URI: $uri")
            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                Log.e(TAG, "[openFileReader] Failed to open input stream for $contentUri - contentResolver returned null")
                throw Exception("Failed to open input stream - contentResolver returned null")
            }
            Log.d(TAG, "[openFileReader] Successfully opened input stream")

            // Try to get file size
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        fileSize = it.getLong(sizeIndex)
                        Log.d(TAG, "[openFileReader] File size from cursor: $fileSize")
                    } else {
                        Log.w(TAG, "[openFileReader] SIZE column not found in cursor")
                    }
                }
            } ?: Log.w(TAG, "[openFileReader] Cursor is null, cannot determine file size")

            val session = FileReaderSession(
                sessionId = sessionId,
                inputStream = inputStream,
                contentUri = contentUri,
                position = 0,
                fileSize = fileSize
            )

            fileReaders[sessionId] = session
            Log.d(TAG, "[openFileReader] Opened file reader session $sessionId for $contentUri, size: $fileSize")
        } catch (e: Exception) {
            Log.e(TAG, "[openFileReader] Error opening file reader for $contentUri: ${e.message}", e)
            throw e
        }

        return Pair(sessionId, fileSize)
    }

    fun readFile(sessionId: Long, size: Int): ReadResult {
        Log.d(TAG, "[readFile] Reading from session $sessionId, size: $size")
        val session = fileReaders[sessionId]

        if (session == null) {
            Log.e(TAG, "[readFile] Invalid session ID: $sessionId, active sessions: ${fileReaders.keys}")
            return ReadResult(false, null, 0, false, "INVALID_SESSION")
        }

        return try {
            val buffer = ByteArray(size)
            val bytesRead = session.inputStream.read(buffer)
            Log.d(TAG, "[readFile] Read $bytesRead bytes from session $sessionId")

            if (bytesRead > 0) {
                session.position += bytesRead
                val dataToSend = if (bytesRead < size) {
                    buffer.copyOf(bytesRead)
                } else {
                    buffer
                }
                val base64Data = Base64.encodeToString(dataToSend, Base64.NO_WRAP)
                Log.d(TAG, "[readFile] Successfully encoded $bytesRead bytes to base64")
                ReadResult(true, base64Data, bytesRead, false, null)
            } else {
                // EOF reached
                Log.d(TAG, "[readFile] EOF reached for session $sessionId")
                ReadResult(true, null, 0, true, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "[readFile] Error reading from session $sessionId: ${e.message}", e)
            ReadResult(false, null, 0, false, "READ_ERROR: ${e.message}")
        }
    }

    fun closeFileReader(sessionId: Long): Boolean {
        val session = fileReaders.remove(sessionId)

        return if (session != null) {
            try {
                session.inputStream.close()
                Log.d(TAG, "Closed file reader session $sessionId")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error closing session $sessionId", e)
                false
            }
        } else {
            Log.e(TAG, "Attempted to close invalid session ID: $sessionId")
            false
        }
    }

    fun seekFile(sessionId: Long, position: Long): SeekResult {
        val session = fileReaders[sessionId]

        if (session == null) {
            Log.e(TAG, "Invalid session ID: $sessionId")
            return SeekResult(false, 0, "INVALID_SESSION")
        }

        return try {
            // Close current stream and reopen at new position
            val uri = Uri.parse(session.contentUri)
            session.inputStream.close()

            val newInputStream = context.contentResolver.openInputStream(uri)
            if (newInputStream == null) {
                return SeekResult(false, session.position, "SEEK_ERROR: Failed to reopen stream")
            }

            // Skip bytes to reach desired position
            var skipped = 0L
            while (skipped < position) {
                val skipResult = newInputStream.skip(position - skipped)
                if (skipResult <= 0) break
                skipped += skipResult
            }

            // Update session with new stream and position
            val updatedSession = session.copy(
                inputStream = newInputStream,
                position = skipped
            )
            fileReaders[sessionId] = updatedSession

            Log.d(TAG, "Seeked session $sessionId to position $skipped")
            SeekResult(true, skipped, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking in session $sessionId", e)
            SeekResult(false, session.position, "SEEK_ERROR: ${e.message}")
        }
    }

    fun readToEnd(sessionId: Long): ReadResult {
        val session = fileReaders[sessionId]

        if (session == null) {
            Log.e(TAG, "Invalid session ID: $sessionId")
            return ReadResult(false, null, 0, false, "INVALID_SESSION")
        }

        return try {
            val buffer = mutableListOf<Byte>()
            var totalBytesRead = 0
            val tempBuffer = ByteArray(8192)

            while (true) {
                val bytesRead = session.inputStream.read(tempBuffer)
                if (bytesRead <= 0) break

                for (i in 0 until bytesRead) {
                    buffer.add(tempBuffer[i])
                }
                totalBytesRead += bytesRead
            }

            session.position += totalBytesRead

            if (totalBytesRead > 0) {
                val dataBytes = buffer.toByteArray()
                val base64Data = Base64.encodeToString(dataBytes, Base64.NO_WRAP)
                ReadResult(true, base64Data, totalBytesRead, true, null)
            } else {
                ReadResult(true, null, 0, true, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading to end from session $sessionId", e)
            ReadResult(false, null, 0, false, "READ_ERROR: ${e.message}")
        }
    }

    fun getFileReaderInfo(sessionId: Long): SessionInfo? {
        val session = fileReaders[sessionId] ?: return null

        return SessionInfo(
            sessionId = session.sessionId,
            contentUri = session.contentUri,
            position = session.position,
            fileSize = session.fileSize,
            isOpen = true
        )
    }
}
