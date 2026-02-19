package com.plugin.android.mediastore

import android.app.Activity
import android.util.Log
import app.tauri.plugin.JSArray
import app.tauri.plugin.JSObject

class Example(private val activity: Activity) {
    private val mediaStoreHelper = MediaStoreHelper(activity)
    private val TAG = "Example"

    fun pong(value: String): String {
        Log.i("Pong", value)
        return value
    }

    fun getAudioFiles(): JSObject {
        val audioFiles = mediaStoreHelper.getAudioFiles()
        val filesArray = JSArray()

        audioFiles.forEach { file ->
            val fileObj = JSObject()
            fileObj.put("id", file.id)
            fileObj.put("title", file.title)
            fileObj.put("artist", file.artist)
            fileObj.put("album", file.album)
            fileObj.put("duration", file.duration)
            fileObj.put("contentUri", file.contentUri)
            fileObj.put("firstFourBytes", file.firstFourBytes)
            filesArray.put(fileObj)
        }

        val ret = JSObject()
        ret.put("files", filesArray)

        Log.d(TAG, "Returning ${audioFiles.size} audio files")
        return ret
    }

    fun openFileReader(contentUri: String): JSObject {
        val ret = JSObject()
        try {
            val (sessionId, fileSize) = mediaStoreHelper.openFileReader(contentUri)
            ret.put("success", true)
            ret.put("sessionId", sessionId)
            ret.put("fileSize", fileSize)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file reader", e)
            ret.put("success", false)
            ret.put("error", e.message)
        }
        return ret
    }

    fun readFile(sessionId: Long, size: Int): JSObject {
        val ret = JSObject()
        val result = mediaStoreHelper.readFile(sessionId, size)
        ret.put("success", result.success)
        ret.put("data", result.data)
        ret.put("bytesRead", result.bytesRead)
        ret.put("isEof", result.isEof)
        ret.put("error", result.error)
        return ret
    }

    fun closeFileReader(sessionId: Long): JSObject {
        val ret = JSObject()
        val closed = mediaStoreHelper.closeFileReader(sessionId)
        ret.put("success", closed)
        if (!closed) {
            ret.put("error", "FAILED_TO_CLOSE")
        }
        return ret
    }

    fun seekFile(sessionId: Long, position: Long): JSObject {
        val ret = JSObject()
        val result = mediaStoreHelper.seekFile(sessionId, position)
        ret.put("success", result.success)
        ret.put("newPosition", result.newPosition)
        ret.put("error", result.error)
        return ret
    }

    fun readToEnd(sessionId: Long): JSObject {
        val ret = JSObject()
        val result = mediaStoreHelper.readToEnd(sessionId)
        ret.put("success", result.success)
        ret.put("data", result.data)
        ret.put("bytesRead", result.bytesRead)
        ret.put("isEof", result.isEof)
        ret.put("error", result.error)
        return ret
    }

    fun getFileReaderInfo(sessionId: Long): JSObject {
        val ret = JSObject()
        val info = mediaStoreHelper.getFileReaderInfo(sessionId)
        if (info != null) {
            ret.put("sessionId", info.sessionId)
            ret.put("contentUri", info.contentUri)
            ret.put("position", info.position)
            ret.put("fileSize", info.fileSize)
            ret.put("isOpen", info.isOpen)
        } else {
            ret.put("error", "SESSION_NOT_FOUND")
        }
        return ret
    }
}
