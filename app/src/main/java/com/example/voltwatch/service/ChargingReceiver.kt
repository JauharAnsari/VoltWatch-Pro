package com.example.voltwatch.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import com.example.voltwatch.R

class ChargingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_POWER_CONNECTED) {
            Log.d("ChargingReceiver", "Power Connected Playing audio")
            context?.let { ctx ->
                try {
                    val mediaPlayer = MediaPlayer.create(ctx, R.raw.voltwatch_charging_audio)
                    if (mediaPlayer != null) {
                        mediaPlayer.setOnCompletionListener { mp ->
                            mp.release()
                            Log.d("ChargingReceiver", "Audio playing completed.")
                        }
                        mediaPlayer.start()
                    } else {
                        Log.e("ChargingReceiver", "Failed to run the audio")
                    }
                } catch (e: Exception) {
                    Log.e("ChargingReceiver", "Error playing audio: ${e.message}")
                }
            }
        }
    }
}
