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

@InvokeArg
class FileReaderOpenArgs {
  var contentUri: String? = null
}

@InvokeArg
class FileReaderReadArgs {
  var sessionId: Long = 0
  var size: Int = 8192
}

@InvokeArg
class FileReaderCloseArgs {
  var sessionId: Long = 0
}

@InvokeArg
class FileReaderSeekArgs {
  var sessionId: Long = 0
  var position: Long = 0
}

@InvokeArg
class FileReaderReadToEndArgs {
  var sessionId: Long = 0
}

@InvokeArg
class FileReaderInfoArgs {
  var sessionId: Long = 0
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

    @Command
    fun openFileReader(invoke: Invoke) {
        val args = invoke.parseArgs(FileReaderOpenArgs::class.java)
        val contentUri = args.contentUri

        if (contentUri == null) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", "INVALID_ARGUMENTS")
            invoke.resolve(ret)
            return
        }

        val result = implementation.openFileReader(contentUri)
        invoke.resolve(result)
    }

    @Command
    fun readFile(invoke: Invoke) {
        val args = invoke.parseArgs(FileReaderReadArgs::class.java)
        val result = implementation.readFile(args.sessionId, args.size)
        invoke.resolve(result)
    }

    @Command
    fun closeFileReader(invoke: Invoke) {
        val args = invoke.parseArgs(FileReaderCloseArgs::class.java)
        val result = implementation.closeFileReader(args.sessionId)
        invoke.resolve(result)
    }

    @Command
    fun seekFile(invoke: Invoke) {
        val args = invoke.parseArgs(FileReaderSeekArgs::class.java)
        val result = implementation.seekFile(args.sessionId, args.position)
        invoke.resolve(result)
    }

    @Command
    fun readToEnd(invoke: Invoke) {
        val args = invoke.parseArgs(FileReaderReadToEndArgs::class.java)
        val result = implementation.readToEnd(args.sessionId)
        invoke.resolve(result)
    }

    @Command
    fun getFileReaderInfo(invoke: Invoke) {
        val args = invoke.parseArgs(FileReaderInfoArgs::class.java)
        val result = implementation.getFileReaderInfo(args.sessionId)
        invoke.resolve(result)
    }
}
