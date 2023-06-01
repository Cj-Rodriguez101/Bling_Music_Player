package com.cjrodriguez.blingmusicplayer.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MediaScanBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {nonNullIntent->
                Log.e("yes","change detected here ${nonNullIntent.action.toString()}")
        }
    }
}