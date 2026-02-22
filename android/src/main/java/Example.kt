package com.plugin.android.mediastore

import android.app.Activity
import android.util.Log

class Example(private val activity: Activity) {
    private val TAG = "Example"

    fun pong(value: String): String {
        Log.i("Pong", value)
        return value
    }
}
