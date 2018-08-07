package com.example.jacek.kalendarz

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity



class Autorun : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val intent = Intent(context, MainActivity::class.java)

        context?.startActivity(intent)
    }
}