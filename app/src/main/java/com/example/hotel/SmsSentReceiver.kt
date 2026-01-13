package com.example.hotel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (resultCode) {
            android.app.Activity.RESULT_OK ->
                Toast.makeText(context, "SMS sent successfully!", Toast.LENGTH_SHORT).show()
            else ->
                Toast.makeText(context, "Failed to send SMS!", Toast.LENGTH_SHORT).show()
        }
    }
}
