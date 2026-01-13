package com.example.hotel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class SmsDeliveredReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "SMS delivered!", Toast.LENGTH_SHORT).show()
    }
}
