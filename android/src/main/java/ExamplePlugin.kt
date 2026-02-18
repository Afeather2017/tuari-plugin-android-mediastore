package com.plugin.android.mediastore

import android.app.Activity
import android.Manifest
import android.util.Log
import app.tauri.annotation.Command
import app.tauri.annotation.InvokeArg
import app.tauri.annotation.Permission
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import app.tauri.plugin.Invoke

@InvokeArg
class PingArgs {
  var value: String? = null
}

@TauriPlugin(
    permissions = [
        Permission(
            strings = [Manifest.permission.READ_MEDIA_AUDIO],
            alias = "audio"
        ),
        Permission(
            strings = [Manifest.permission.READ_EXTERNAL_STORAGE],
            alias = "storage"
        )
    ]
)
class ExamplePlugin(private val activity: Activity): Plugin(activity) {
    private val implementation = Example(activity)
    private val TAG = "ExamplePlugin"

    @Command
    fun ping(invoke: Invoke) {
        val args = invoke.parseArgs(PingArgs::class.java)

        val ret = JSObject()
        ret.put("value", implementation.pong(args.value ?: "default value :("))
        invoke.resolve(ret)
    }

    @Command
    fun getAudioFiles(invoke: Invoke) {
        Log.i(TAG, "Getting audio files...")
        val result = implementation.getAudioFiles()
        invoke.resolve(result)
    }
}
