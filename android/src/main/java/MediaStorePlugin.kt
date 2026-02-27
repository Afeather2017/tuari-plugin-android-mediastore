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
import kotlinx.coroutines.*

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

@InvokeArg
class GetAudioFilesArgs {
  var excludeSuffixes: List<String>? = null
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
class MediaStorePlugin(private val activity: Activity): Plugin(activity) {
    private val example = Example(activity)
    private val commands = MediaStoreCommands(activity)
    private val TAG = "MediaStorePlugin"

    @Command
    fun ping(invoke: Invoke) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val args = invoke.parseArgs(PingArgs::class.java)
                val ret = JSObject()
                ret.put("value", example.pong(args.value ?: "default value :("))
                invoke.resolve(ret)
            } catch (e: Exception) {
                invoke.reject(e.message ?: "Unknown error")
            }
        }
    }

    @Command
    fun getAudioFiles(invoke: Invoke) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Getting audio files...")
                val args = invoke.parseArgs(GetAudioFilesArgs::class.java)
                val result = commands.getAudioFiles(args.excludeSuffixes)
                invoke.resolve(result)
            } catch (e: Exception) {
                invoke.reject(e.message ?: "Unknown error")
            }
        }
    }

    @Command
    fun openFileReader(invoke: Invoke) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "[openFileReader] Command invoked")
                val args = invoke.parseArgs(FileReaderOpenArgs::class.java)
                val contentUri = args.contentUri
                Log.d(TAG, "[openFileReader] Parsed args - contentUri: $contentUri")

                if (contentUri == null) {
                    Log.e(TAG, "[openFileReader] contentUri is null!")
                    val ret = JSObject()
                    ret.put("success", false)
                    ret.put("error", "INVALID_ARGUMENTS")
                    invoke.resolve(ret)
                    return@launch
                }

                val result = commands.openFileReader(contentUri)
                Log.d(TAG, "[openFileReader] Result: success=${result.get("success")}, sessionId=${result.get("sessionId")}")
                invoke.resolve(result)
            } catch (e: Exception) {
                invoke.reject(e.message ?: "Unknown error")
            }
        }
    }

    @Command
    fun readFile(invoke: Invoke) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "[readFile] Command invoked")
                val args = invoke.parseArgs(FileReaderReadArgs::class.java)
                Log.d(TAG, "[readFile] Parsed args - sessionId: ${args.sessionId}, size: ${args.size}")
                val result = commands.readFile(args.sessionId, args.size)
                Log.d(TAG, "[readFile] Result: success=${result.get("success")}, bytesRead=${result.get("bytesRead")}")
                invoke.resolve(result)
            } catch (e: Exception) {
                invoke.reject(e.message ?: "Unknown error")
            }
        }
    }

    @Command
    fun closeFileReader(invoke: Invoke) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "[closeFileReader] Command invoked")
                val args = invoke.parseArgs(FileReaderCloseArgs::class.java)
                Log.d(TAG, "[closeFileReader] Parsed args - sessionId: ${args.sessionId}")
                val result = commands.closeFileReader(args.sessionId)
                Log.d(TAG, "[closeFileReader] Result: success=${result.get("success")}")
                invoke.resolve(result)
            } catch (e: Exception) {
                invoke.reject(e.message ?: "Unknown error")
            }
        }
    }

    @Command
    fun seekFile(invoke: Invoke) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val args = invoke.parseArgs(FileReaderSeekArgs::class.java)
                val result = commands.seekFile(args.sessionId, args.position)
                invoke.resolve(result)
            } catch (e: Exception) {
                invoke.reject(e.message ?: "Unknown error")
            }
        }
    }

    @Command
    fun readToEnd(invoke: Invoke) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val args = invoke.parseArgs(FileReaderReadToEndArgs::class.java)
                val result = commands.readToEnd(args.sessionId)
                invoke.resolve(result)
            } catch (e: Exception) {
                invoke.reject(e.message ?: "Unknown error")
            }
        }
    }

    @Command
    fun getFileReaderInfo(invoke: Invoke) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val args = invoke.parseArgs(FileReaderInfoArgs::class.java)
                val result = commands.getFileReaderInfo(args.sessionId)
                invoke.resolve(result)
            } catch (e: Exception) {
                invoke.reject(e.message ?: "Unknown error")
            }
        }
    }
}
