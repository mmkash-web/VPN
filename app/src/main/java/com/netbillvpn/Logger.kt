package com.netbillvpn

import android.util.Log

object Logger {
    private const val TAG = "NETBILL_VPN"

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}
