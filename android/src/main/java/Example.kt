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
            fileObj.put("filePath", file.filePath)
            filesArray.put(fileObj)
        }

        val ret = JSObject()
        ret.put("files", filesArray)

        Log.d(TAG, "Returning ${audioFiles.size} audio files")
        return ret
    }
}
